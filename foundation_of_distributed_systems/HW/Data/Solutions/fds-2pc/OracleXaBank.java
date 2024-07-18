package ch.unibas.dmi.dbis.fds._2pc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Check the XA stuff here -->
 * https://docs.oracle.com/cd/B14117_01/java.101/b10979/xadistra.htm
 *
 * @author Alexander Stiemer (alexander.stiemer at unibas.ch)
 */
public class OracleXaBank extends AbstractOracleXaBank {

    public OracleXaBank(final String BIC, final String jdbcConnectionString, final String dbmsUsername,
            final String dbmsPassword) throws SQLException {
        super(BIC, jdbcConnectionString, dbmsUsername, dbmsPassword);
    }

    @Override
    public float getBalance(final String iban) throws SQLException {
        // TODO: your turn ;-)
        ResultSet res = null;
        java.sql.Statement statement = null;
        float balance;
        // System.out.println("Debug: getBalance function called");

        try (Connection c = this.getXaConnection().getConnection()) {
            c.setAutoCommit(false);
            statement = c.createStatement();
            // receive balance from the Database
            res = statement.executeQuery("SELECT balance FROM account WHERE IBAN = '" + iban + "'");
            // c.commit();
            if (res.next()) {
                balance = res.getFloat("balance");
            } else {
                // There is no BankAccount with that Iban
                // c.close();
                throw new SQLException("No bankaccount with that IBAN");
            }

            return balance;
        } finally {
            // res.close();
            // statement.close();
        }

        // throw new UnsupportedOperationException( "Implement me :-)" );
    }

    /*
     * Exersice b)
     * 2PC Presumed Abort:
     * We think presumed Abort 2PC can be used.
     * Whether we think it would not be good for the bank to implement it.
     * Because a bank should restart the attempted transaction if the coordinator
     * crashes.
     * But this is not possible because there are no log records. Instead, the
     * action is simply aborted.
     * Especially for a bank, the execution of the transactions should be more
     * important
     * than the performance by reducing the log I/Os.
     * 
     * 2PC Transfer of Coordination:
     * It is also possible here. 2PC Transfer of Coordination could be useful
     * if the original coordinator crashes frequently.
     * Then you would not have to restart everything again,
     * but would only have to wait for the acknowledgement.
     * This would result in a higher fault tolerance.
     * 
     * To implement this, we would have to change line 153,
     * more precisely in the prepare method of Oracle,
     * so that there is the possibility to pass the role of the coordinator,
     * which then receives the responses of the other agent.
     * The code that follows to decide whether the coordinator
     * should be aborted or comitted would then have to be executed on the new
     * coordinator.
     */

    @Override
    public void transfer(final AbstractOracleXaBank TO_BANK, final String ibanFrom, final String ibanTo,
            final float value) {
        // System.out.println("Debug: Started Transfer");
        // TODO: your turn ;-)

        float balanceFrom;
        float balanceTo;
        boolean rollback = false;
        try {
            // check if the value is positive
            if (value <= 0) {
                rollback = true;
                throw new Exception("Not possible to send negative money");
            }
            // check if ibans are valid
            balanceFrom = this.getBalance(ibanFrom);
            balanceTo = TO_BANK.getBalance(ibanTo);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println(e.getMessage());
        }

        Connection withdrawConnection = null;
        Connection depositConnection = null;
        java.sql.Statement withdrawStatement = null;
        java.sql.Statement depositStatement = null;
        try {
            // check if both ibans are the same
            if (ibanFrom.equals(ibanTo) && this == TO_BANK) {
                throw new SQLException("Error: Not Possible to send money to yourself.");
            }

            // create everything
            Xid xid = this.startTransaction();
            Xid bankXid = TO_BANK.startTransaction();
            // Xid bankXid = TO_BANK.startTransaction(xid);

            balanceFrom = this.getBalance(ibanFrom);
            balanceTo = TO_BANK.getBalance(ibanTo);

            // Withdraw Money from Account
            if (balanceFrom - value >= 0) {
                String s = "Update account Set balance = " + (balanceFrom - value) + " Where IBAN = '" + ibanFrom
                        + "'";
                withdrawConnection = this.getXaConnection().getConnection();
                withdrawStatement = withdrawConnection.createStatement();
                withdrawStatement.executeUpdate(s);
            } else {
                rollback = true;
                throw new SQLException("You have not enough money :(");
            }

            // deposit money to another account
            if (balanceTo + value <= 15000) {
                depositConnection = TO_BANK.getXaConnection().getConnection();
                depositStatement = depositConnection.createStatement();
                String s = "Update account set balance = " + (balanceTo + value) + "where IBAN = '" + ibanTo + "'";
                depositStatement.execute(s);
            } else {
                rollback = true;
                // throw new SQLException("You have to much money on your bank account.");
            }

            // close both branches
            this.getXaResource().end(xid, XAResource.TMNOFLAGS);
            TO_BANK.getXaResource().end(bankXid, XAResource.TMNOFLAGS);

            // Prepare the RMs
            int prpWithdraw = this.getXaResource().prepare(xid);
            int prpDeposit = TO_BANK.getXaResource().prepare(bankXid);

            if (!((prpWithdraw == XAResource.XA_OK) || (prpWithdraw == XAResource.XA_RDONLY))) {
                rollback = true;
            }

            if (!((prpDeposit == XAResource.XA_OK) || (prpDeposit == XAResource.XA_RDONLY))) {
                rollback = true;
            }

            if (prpWithdraw == XAResource.XA_OK) {
                this.endTransaction(xid, rollback);
            }

            if (prpDeposit == XAResource.XA_OK) {
                TO_BANK.endTransaction(bankXid, rollback);
            }

        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e.getMessage());
        } finally {
            // close connections

            // withdrawConnection.close();
            // depositConnection.close();
            // withdrawStatement.close();
            // depositStatement.close();

        }

        // throw new UnsupportedOperationException( "Implement me :-)" );
    }
}
