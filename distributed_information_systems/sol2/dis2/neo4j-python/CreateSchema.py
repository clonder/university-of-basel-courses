from neo4j import GraphDatabase


def main():
    clean_constraints = True
    clean_relations = True
    clean_vertices = True

    indexes = True
    load = True
    rel = True
    # Connect to Neo4j
    with GraphDatabase.driver("bolt://localhost:7687", auth=("neo4j", r"Tp6Ruboz%5up")) as driver:
        with driver.session(database="neo4j") as session:

            if clean_constraints == True:
                session.run("DROP CONSTRAINT c_airport_iata IF EXISTS")
                session.run("DROP CONSTRAINT c_carrier_code IF EXISTS")
                session.run("DROP CONSTRAINT c_plane_tailnum IF EXISTS")
                session.run("DROP CONSTRAINT c_cancellation_code IF EXISTS")
                session.run("DROP INDEX flight_carrier IF EXISTS")
                session.run("DROP INDEX flight_tailNum IF EXISTS")
                session.run("DROP INDEX flight_dest IF EXISTS")
                session.run("DROP INDEX flight_origin IF EXISTS")
                session.run("DROP INDEX flight_cancel IF EXISTS")
                session.run("DROP INDEX airport_iata IF EXISTS")
                session.run("DROP INDEX plane_tail IF EXISTS")
                session.run("DROP INDEX carrier_code IF EXISTS")
                session.run("DROP INDEX fuel_carrier IF EXISTS")
                print("Constraints deleted")

            if indexes == True:
                session.execute_write(create_indexes)

            if clean_relations == True:
                session.execute_write(clear_relations)

            if clean_vertices == True:
                clear_vertices(session)

            if load == True:
                session.execute_write(load_airports)
                session.execute_write(load_carriers)
                session.execute_write(load_planes)
                load_flights(session)
                session.execute_write(load_cancellation)
                session.execute_write(load_fuel)

            if rel == True:
                add_rel_flight_plane(session)
                add_rel_flight_airport_origin(session)
                add_rel_flight_airport_dest(session)
                add_rel_flight_carrier(session)
                add_rel_flight_cancellation(session)
                add_rel_carrier_fuel(session)

            session.close()
            driver.close()


def create_indexes(graph):
    query = """
            CREATE CONSTRAINT c_carrier_code
            FOR (carrier:Carrier) REQUIRE carrier.code IS UNIQUE
            """
    graph.run(query)

    query = """
            CREATE CONSTRAINT c_plane_tailnum
            FOR (plane:Plane) REQUIRE plane.tailnum IS UNIQUE
            """
    graph.run(query)

    query = """
            CREATE CONSTRAINT c_cancellation_code
            FOR (cancellation:Cancellation) REQUIRE cancellation.code IS UNIQUE
            """
    graph.run(query)

    query = """
            CREATE CONSTRAINT c_airport_iata
            FOR (airport:Airport) REQUIRE airport.iata IS UNIQUE
            """
    graph.run(query)

    query = """
            CREATE INDEX flight_carrier IF NOT EXISTS FOR (F:Flight) ON (F.uniqueCarrier);
            """
    graph.run(query)

    query = """
            CREATE INDEX flight_tailNum IF NOT EXISTS FOR (F:Flight) ON (F.tailNum);
            """
    graph.run(query)

    query = """
            CREATE INDEX flight_dest IF NOT EXISTS  FOR (F:Flight) ON (F.dest);
            """
    graph.run(query)

    query = """
            CREATE INDEX flight_origin IF NOT EXISTS FOR (F:Flight) ON (F.origin);
            """
    graph.run(query)

    query = """
            CREATE INDEX flight_cancel IF NOT EXISTS FOR (F:Flight) ON (F.cancellationCode);
            """
    graph.run(query)

    query = """
            CREATE INDEX airport_iata IF NOT EXISTS FOR (A:Airport) ON (A.iata);
            """
    graph.run(query)

    query = """
            CREATE INDEX plane_tail IF NOT EXISTS FOR (P:Plane) ON (P.tailNum);
            """
    graph.run(query)

    query = """
            CREATE INDEX carrier_code IF NOT EXISTS FOR (C:Carrier) ON (C.code);
            """
    graph.run(query)

    query = """
            CREATE INDEX fuel_carrier IF NOT EXISTS FOR (F:Fuel) ON (F.uniqueCarrier);
            """
    graph.run(query)

    print("Constraints created")


def load_airports(graph):
    query = """
            // REMARK: CSV must be in the import folder
            // IMPORT Load additional data
            // load airports
            LOAD CSV WITH HEADERS FROM 'file:///15729-DIS-ex2-Dataset/Additional_Data/airports.csv'
            AS row
            CREATE (:Airport {
              iata:    row.iata,
              airport: row.airport,
              city:    row.city,
              state:   row.state,
              country: row.country
            // lat:     row.lat,
            // long:    row.long
            })
            """

    graph.run(query)

    print("Airports loaded")


def delete_airports(graph):
    query = """
            // Delete all vertices
            MATCH (n:Airport)
            DETACH DELETE n
            """
    graph.run(query)
    print("Airports deleted")


def load_carriers(graph):
    query = """
            // load carriers
            LOAD CSV WITH HEADERS FROM 'file:///15729-DIS-ex2-Dataset/Additional_Data/carriers.csv'
            AS row
            CREATE (:Carrier {
              code:        row.Code,
              description: row.Description
            })
            """
    graph.run(query)
    print("Carriers loaded")


def delete_carriers(graph):
    query = """
            // Delete all vertices
            MATCH (n:Carrier)
            DETACH DELETE n
            """
    graph.run(query)
    print("Carriers deleted")


def load_planes(graph):
    query = """
            // load planes
            LOAD CSV WITH HEADERS FROM 'file:///15729-DIS-ex2-Dataset/Additional_Data/plane-data.csv'
            AS row
            CREATE (:Plane {
              tailnum:       row.tailnum,
              type:          row.type,
              manufacturer:  row.manufacturer,
              issue_date:    row.issue_date,
              model:         row.model,
              status:        row.status,
              aircraft_type: row.aircraft_type,
              engine_type:   row.engine_type,
              year:          row.year
            })
            """
    graph.run(query)

    print("Planes loaded")


def delete_planes(graph):
    query = """
            // Delete all vertices
            MATCH (n:Plane)
            DETACH DELETE n
            """
    graph.run(query)
    print("Planes deleted")


def load_flights(session):
    months = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
    years = [2014, 2015, 2016]
    for year in years:
        for month in months:
            query = f"""
                    // load weather
                    // for 1 to 12 and 2014 to 2016
                    LOAD CSV WITH HEADERS FROM 'file:///15729-DIS-ex2-Dataset/{year}/On_Time_On_Time_Performance_{year}_{month}.csv'
                    AS row
                    CREATE (:Flight {{
                        year:                 toInteger(row.Year),
                        month:                toInteger(row.Month),
                          day:                  toInteger(row.day),
                          uniqueCarrier:        row.UniqueCarrier,
                          airlineID:            row.AirlineID,
                          carrier:              row.Carrier,
                          tailNum:              row.TailNum,
                          flightNum:            row.FlightNum,
                          origin:               row.Origin,
                          dest:                 row.Dest,
                          destCityName:         row.DestCityName,
                          destState:            row.DestState,
                          depTime:              toInteger(row.DepTime),
                          depDelay:             toInteger(row.DepDelay),
                          depDelayMinutes:      row.DepDelayMinutes,
                          taxiOut:              toInteger(row.TaxiOut),
                          wheelsOff:            row.WheelsOff,
                          wheelsOn:             row.WheelsOn,
                          taxiIn:               toInteger(row.TaxiIn),
                          cancelled:            row.Cancelled,
                          cancellationCode:     row.CancellationCode,
                          airTime:               toInteger(row.AirTime),
                          flights:              row.Flights,
                          distance:             toFloat(row.Distance),
                          arrDelay: toInteger(row.ArrDelayMinutes),
                          arrTime: toInteger(row.ArrTime)
                    }})
                    """
            session.run(query)

            print("Flights loaded for month " + str(month))
        print("Flights loaded for year " + str(year))
    print("Flights loaded")


def delete_flights(session):
    query = """
            MATCH (n:Flight)
            CALL { WITH n 
            DETACH DELETE n
            } IN TRANSACTIONS OF 50000 ROWS;
            """
    session.run(query)
    print("Flights deleted")


def load_cancellation(graph):
    query = """
            // Load cancelled flights
            LOAD CSV WITH HEADERS FROM 'file:///15729-DIS-ex2-Dataset/Lookup_Tables/L_CANCELLATION.csv'
            AS row
            CREATE (:Cancellation {
              code:        row.Code,
              description: row.Description
            })
            """
    graph.run(query)

    print("Cancellations loaded")


def delete_cancellation(graph):
    query = """
            // Delete all vertices
            MATCH (n:Cancellation)
            DETACH DELETE n
            """
    graph.run(query)
    print("Cancellations deleted")


def load_fuel(graph):
    years = [2014, 2015, 2016]
    for year in years:
        query = f"""
                // Load fuel statistics
                // YEAR	QUARTER	MONTH	AIRLINE_ID	UNIQUE_CARRIER	CARRIER	CARRIER_NAME	CARRIER_GROUP_NEW	SALA_GALLONS	SDOM_GALLONS	SDOMT_GALLONS	SATL_GALLONS	SPAC_GALLONS	SLAT_GALLONS	SINT_GALLONS	TS_GALLONS	NALA_GALLONS	NDOM_GALLONS	NDOMT_GALLONS	NATL_GALLONS	NPAC_GALLONS	NLAT_GALLONS	MAC_GALLONS	NINT_GALLONS	TN_GALLONS	TDOMT_GALLONS	TINT_GALLONS	TOTAL_GALLONS	SALA_COST	SDOM_COST	SDOMT_COST	SATL_COST	SPAC_COST	SLAT_COST	SINT_COST	TS_COST	NALA_COST	NDOM_COST	NDOMT_COST	NATL_COST	NPAC_COST	NLAT_COST	MAC_COST	NINT_COST	TN_COST	TDOMT_COST	TINT_COST	TOTAL_COST
                LOAD CSV WITH HEADERS FROM 'file:///15729-DIS-ex2-Dataset/Additional_Data/monthly-fuel-statistics-{str(year)}.csv'
                AS row
                CREATE (:Fuel {{
                  year:               toInteger(row.YEAR),
                  quarter:            toInteger(row.QUARTER),
                  month:              toInteger(row.MONTH),
                  airlineID:          row.AIRLINE_ID,
                  uniqueCarrier:      row.UNIQUE_CARRIER,
                  carrier:            row.CARRIER,
                  carrierName:        row.CARRIER_NAME,
                  totalGallons:       row.TOTAL_GALLONS,
                  totalCost:          row.TOTAL_COST
                }})
                """
        graph.run(query)
        print("Fuel loaded for year " + str(year))
    print("Fuel loaded")


def delete_fuel(graph):
    query = """
            // Delete all vertices
            MATCH (n:Fuel)
            DETACH DELETE n
            """
    graph.run(query)
    print("Fuel deleted")


def add_rel_flight_plane(session):
    query = """
            MATCH (F:Flight)
            CALL {
            WITH F
            MATCH (P:Plane)
              WHERE F.tailNum = P.tailnum
            CREATE (F)-[r:executedWith]->(P)
            } IN TRANSACTIONS OF 500000 ROWS
            """
    session.run(query)
    print("Flight -> Plane relationship added")


def delete_rel_flight_plane(graph):
    query = """
            MATCH (F:Flight)-[r:executedWith]->(P:Plane)
            DELETE r
            """
    graph.run(query)
    print("Flight -> Plane relationship deleted")


def add_rel_flight_airport_origin(session):
    query = """
            MATCH (F:Flight)
            CALL {
            WITH F
            MATCH (A:Airport)
              WHERE F.origin = A.iata
            CREATE (F)-[r:fromAirport]->(P)
            } IN TRANSACTIONS OF 50000 ROWS
            """
    session.run(query)
    print("Flight -> Origin Airport relationship added")


def delete_rel_flight_airport_origin(graph):
    query = """
            MATCH (F:Flight)-[r:fromAirport]->(A:Airport)
            DELETE r
            """
    graph.run(query)
    print("Flight -> Origin Airport relationship deleted")


def add_rel_flight_airport_dest(session):
    query = """
            //  Flight -> Destination Airport
            MATCH (F:Flight)
            CALL {
            WITH F
            MATCH (A:Airport)
              WHERE F.dest = A.iata
            CREATE (F)-[r:to]->(P)
            } IN TRANSACTIONS OF 50000 ROWS
            """
    session.run(query)
    print("Flight -> Destination Airport relationship added")


def delete_rel_flight_airport_dest(graph):
    query = """
            MATCH (F:Flight)-[r:to]->(A:Airport)
            DELETE r
            """
    graph.run(query)
    print("Flight -> Destination Airport relationship deleted")


def add_rel_flight_carrier(session):
    query = """
            // Flight -> Carrier
            MATCH (F:Flight)
            CALL {
            WITH F
            MATCH (C:Carrier)
              WHERE F.uniqueCarrier = C.code
            CREATE (F)-[r:performedBy]->(C)
            } IN TRANSACTIONS OF 50000 ROWS
            """
    session.run(query)
    print("Flight -> Carrier relationship added")


def delete_rel_flight_carrier(session):
    query = """
            MATCH (F:Flight)-[r:performedBy]->(C:Carrier)
            DELETE r
            """
    session.run(query)
    print("Flight -> Carrier relationship deleted")


def add_rel_flight_cancellation(session):
    query = """
            MATCH (F:Flight)
            CALL {
            WITH F
            MATCH (C:Cancellation)
              WHERE F.cancellationCode = C.code
            CREATE (F)-[r:cancelledBy]->(C)
            } IN TRANSACTIONS OF 50000 ROWS
            """
    session.run(query)
    print("Flight -> Cancellation relationship added")


def delete_rel_flight_cancellation(graph):
    query = """
            MATCH (F:Flight)-[r:cancelledBy]->(C:Cancellation)
            DELETE r
            """
    graph.run(query)
    print("Flight -> Cancellation relationship deleted")


def add_rel_carrier_fuel(session):
    query = """
            // Carrier -> Fuel
            MATCH (C:Carrier)
            CALL {
            WITH C
            MATCH (F:Fuel)
              WHERE F.uniqueCarrier = C.code
            CREATE (C)-[r:consumed]->(F)
            } IN TRANSACTIONS OF 50000 ROWS
            """
    session.run(query)
    print("Carrier -> Fuel relationship added")


def delete_rel_carrier_fuel(graph):
    query = """
            MATCH (C:Carrier)-[r:consumed]->(F:Fuel)
            DELETE r
            """
    graph.run(query)
    print("Carrier -> Fuel relationship deleted")


def clear_relations(graph):
    delete_rel_flight_plane(graph)
    delete_rel_flight_airport_origin(graph)
    delete_rel_flight_airport_dest(graph)
    delete_rel_flight_carrier(graph)
    delete_rel_flight_cancellation(graph)
    delete_rel_carrier_fuel(graph)
    print("All relationships deleted")


def clear_vertices(session):
    delete_airports(session)
    delete_carriers(session)
    delete_planes(session)
    delete_flights(session)
    delete_cancellation(session)
    delete_fuel(session)
    print("All data deleted")


if __name__ == "__main__":
    main()
