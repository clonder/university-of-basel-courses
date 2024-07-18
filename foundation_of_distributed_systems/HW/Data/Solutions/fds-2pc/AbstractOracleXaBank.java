package ch.unibas.dmi.dbis.fds._2pc;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import oracle.jdbc.xa.OracleXid;
import oracle.jdbc.xa.client.OracleXADataSource;

/**
 * Check the XA stuff here -->
 * https://docs.oracle.com/cd/B14117_01/java.101/b10979/xadistra.htm
 *
 * @author Alexander Stiemer (alexander.stiemer at unibas.ch)
 */
public abstract class AbstractOracleXaBank {

    // Load database driver
    static {
        try {
            java.sql.DriverManager.registerDriver(new oracle.jdbc.OracleDriver()); // Alternatively:
                                                                                   // Class.forName("oracle.jdbc.OracleDriver");
        } catch (SQLException ex) {
            throw new InternalError("Exception registering the Oracle database driver.", ex);
        }
    }

    protected static final Logger LOG = Logger.getLogger(AbstractOracleXaBank.class.getName());

    // Xid components
    private static final int formatIdentifier = 0;
    private static final Object globalTransactionIdLock = new Object();
    private static BigInteger globalTransactionId = BigInteger.ZERO;

    //
    public final String BIC;
    protected final byte[] branchQualifier;

    //
    public final String jdbcConnectionString;

    // XA components
    private XAConnection xaConnection;
    private XAResource xaResource;

    public AbstractOracleXaBank(final String BIC, final String jdbcConnectionString, final String dbmsUsername,
            final String dbmsPassword) throws SQLException {
        this.BIC = BIC;
        this.jdbcConnectionString = jdbcConnectionString;

        this.branchQualifier = ByteBuffer.allocate(64).putInt(this.BIC.hashCode()).array();

        this.xaConnection = openConnection(jdbcConnectionString, dbmsUsername, dbmsPassword);
        this.xaResource = this.xaConnection.getXAResource();

        rollbackPendingTransactions();

        setupDatabaseTables();
    }

    public abstract float getBalance(final String iban) throws SQLException;

    public abstract void transfer(AbstractOracleXaBank TO_BANK, String ibanFrom, String ibanTo, float value);

    public XAConnection openConnection(final String connectionString, final String dbmsUsername,
            final String dbmsPassword) throws SQLException {
        final OracleXADataSource dataSource = new OracleXADataSource();
        dataSource.setURL(connectionString);
        dataSource.setUser(dbmsUsername);
        dataSource.setPassword(dbmsPassword);

        return dataSource.getXAConnection();
    }

    public final void closeConnection() {
        try {
            if (this.xaConnection != null) {
                this.xaConnection.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            this.xaConnection = null;
        }
    }

    public XAConnection getXaConnection() {
        return xaConnection;
    }

    public XAResource getXaResource() {
        return xaResource;
    }

    public Xid startTransaction() throws XAException {
        final Xid xid = this.getXid();
        // System.out.println("Debug: We started a transaction");
        // TODO: your turn ;-)
        XAResource oxar1 = getXaResource();
        oxar1.start(xid, XAResource.TMNOFLAGS);
        return xid;

        // throw new UnsupportedOperationException( "Implement me :-)" );
    }

    public Xid startTransaction(final Xid globalTransactionId) throws XAException {
        final Xid xid = this.getXid(globalTransactionId);

        // TODO: your turn ;-)
        XAResource oxar1 = getXaResource();
        oxar1.start(xid, XAResource.TMNOFLAGS);
        return xid;

        // throw new UnsupportedOperationException( "Implement me :-)" );
    }

    public void endTransaction(final Xid transactionId, final boolean rollback) throws XAException {

        // TODO: your turn ;-)
        // final Xid xid = this.getXid(transactionId);
        XAResource oxar1 = getXaResource();
        if (rollback) {
            oxar1.rollback(transactionId);
        } else {
            oxar1.commit(transactionId, false);
        }

        // throw new UnsupportedOperationException( "Implement me :-)" );
    }

    public Xid getXid() throws XAException {
        return this.getXid(getNewGlobalTransactionId());
    }

    public Xid getXid(final Xid globalTransactionId) throws XAException {
        return this.getXid(globalTransactionId.getGlobalTransactionId());
    }

    public Xid getXid(final byte[] globalTransactionId) throws XAException {
        return new OracleXid(AbstractOracleXaBank.formatIdentifier, globalTransactionId, this.branchQualifier);
    }

    private byte[] getNewGlobalTransactionId() {
        BigInteger globalTransactionId;
        synchronized (globalTransactionIdLock) {
            globalTransactionId = AbstractOracleXaBank.globalTransactionId.add(BigInteger.ONE);
            if (globalTransactionId.bitCount() > 64L * Byte.SIZE) {
                globalTransactionId = BigInteger.ZERO;
            }
            AbstractOracleXaBank.globalTransactionId = globalTransactionId;
        }

        return ByteBuffer.allocate(64).put(globalTransactionId.toByteArray()).array();
    }

    private void rollbackPendingTransactions() {
        try {
            Xid[] transactionIds = xaResource.recover(XAResource.TMENDRSCAN);
            if (transactionIds.length == 0) {
                return;
            }

            LOG.log(Level.INFO,
                    "Found " + transactionIds.length + " pending transactions. Performing a rollback for those.");
            for (Xid transactionId : transactionIds) {
                xaResource.rollback(transactionId);
            }
        } catch (XAException ex) {
            LOG.log(Level.WARNING, "Could not rollback all pending transactions.", ex);
        }
    }

    private void setupDatabaseTables() throws SQLException {
        // System.out.println("Debug: Setup Database");
        try (Connection c = this.xaConnection.getConnection()) {
            c.setAutoCommit(false);

            try {
                final Statement statement = c.createStatement();
                statement.execute("DROP TABLE account");
                c.commit();
            } catch (SQLException ignored) {
            }

            try {
                final Statement statement = c.createStatement();
                statement.execute("DROP TABLE customer");
                c.commit();
            } catch (SQLException ignored) {
            }

            try {
                final Statement statement = c.createStatement();
                statement.execute("CREATE TABLE customer (" +
                        "CustomerNo INTEGER PRIMARY KEY," +
                        "Surname VARCHAR2(50)," +
                        "FirstName VARCHAR2(50)," +
                        "Nation VARCHAR2(50)," +
                        "DateOfBirth DATE," +
                        "Street VARCHAR2(50)," +
                        "ZIP VARCHAR2(5)," +
                        "City VARCHAR2(50)" + ")");

                statement.execute(
                        "INSERT INTO customer VALUES (1, 'Estermann', 'Xaver' , 'CH', to_date('1943/05/03', 'yyyy/mm/dd'), 'Bahnhofstrasse 10a', '8000', 'Zurich')");
                statement.execute(
                        "INSERT INTO customer VALUES (2, 'Martelli', 'Katrin' , 'CH', to_date('1983/12/20', 'yyyy/mm/dd'), 'Dolder 6', '8010', 'Zurich')");
                statement.execute(
                        "INSERT INTO customer VALUES (3, 'Metzler', 'Ruth' , 'CH', to_date('1966/07/30', 'yyyy/mm/dd'), 'Bergstrasse 43', '7234', 'Appenzell')");
                statement.execute(
                        "INSERT INTO customer VALUES (4, 'Deiss', 'Joseph' , 'CH', to_date('1975/01/02', 'yyyy/mm/dd'), 'Rue Victoire 34', '1234', 'Fribourg')");
                statement.execute(
                        "INSERT INTO customer VALUES (5, 'Cotti', 'Flavio' , 'CH', to_date('1967/10/13', 'yyyy/mm/dd'), 'Via Grande 55', '3224', 'Lugano')");
                c.commit();
            } finally {
            }

            try {
                final Statement statement = c.createStatement();
                statement.execute("CREATE TABLE account (" +
                        "IBAN VARCHAR2(50)," +
                        "CustomerNo INTEGER," +
                        "Balance NUMBER," +
                        "InterestRate NUMBER," +
                        "CONSTRAINT pk_account PRIMARY KEY(IBAN)," +
                        "CONSTRAINT fk_customer FOREIGN KEY (CustomerNo) REFERENCES Customer(CustomerNo)," +
                        "CONSTRAINT ck_balance CHECK (Balance >= 0)," +
                        "CONSTRAINT ck_full_account CHECK (Balance <= 15000))"); // CAUTION: Weird bank - accounts have
                                                                                 // a maximum capacity!

                statement.execute("INSERT INTO account VALUES ('CH5367B1', 1, 8000, 0.01 )");
                statement.execute("INSERT INTO account VALUES ('CH5367B2', 2, 15000, 0.02 )");
                statement.execute("INSERT INTO account VALUES ('CH5367B3', 3, 5000, 0.01 )");
                statement.execute("INSERT INTO account VALUES ('CH5367B4', 4, 1700, 0.02 )");
                statement.execute("INSERT INTO account VALUES ('CH5367B5', 5, 2345, 0.0075 )");
                c.commit();
            } finally {
            }
        }
    }
}
