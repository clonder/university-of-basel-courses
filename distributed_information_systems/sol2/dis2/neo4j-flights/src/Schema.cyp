// Delete all edges
MATCH ()-[r]->()
DELETE r
;

// Delete all vertices
MATCH (n:Airport)
DETACH DELETE n
;
// Delete all vertices
MATCH (n:Carrier)
DETACH DELETE n
;
// Delete all vertices
MATCH (n:Plane)
DETACH DELETE n
;
// Delete all vertices
MATCH (n:Flight)
DETACH DELETE n
;

// Delete all vertices
MATCH (n:Cancellation)
DETACH DELETE n
;

// Delete all vertices
MATCH (n:Fuel)
DETACH DELETE n
;


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
;


CREATE CONSTRAINT airport_iata
FOR (airport:Airport) REQUIRE airport.iata IS UNIQUE

DROP CONSTRAINT airport_iata

// load carriers
LOAD CSV WITH HEADERS FROM 'file:///15729-DIS-ex2-Dataset/Additional_Data/carriers.csv'
AS row
CREATE (:Carrier {
  code:        row.Code,
  description: row.Description
})
;

CREATE CONSTRAINT carrier_code
FOR (carrier:Carrier) REQUIRE carrier.code IS UNIQUE

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
;

CREATE CONSTRAINT plane_tailnum
FOR (plane:Plane) REQUIRE plane.tailnum IS UNIQUE

// load weather
// for 1 to 12 and 2014 to 2016
LOAD CSV WITH HEADERS FROM 'file:///15729-DIS-ex2-Dataset/2014/On_Time_On_Time_Performance_2014_2.csv'
AS row
CREATE (:Flight {
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
  arrDelay:           toInteger(row.ArrDelayMinutes),
  arrTime:                toInteger(row.ArrTime)
}) IN TRANSACTIONS OF 50000 ROWS;
;


// Load cancelled flights
LOAD CSV WITH HEADERS FROM 'file:///15729-DIS-ex2-Dataset/Lookup_Tables/L_CANCELLATION.csv'
AS row
CREATE (:Cancellation {
  code:        row.Code,
  description: row.Description
})

CREATE CONSTRAINT cancellation_code
FOR (cancellation:Cancellation) REQUIRE cancellation.code IS UNIQUE

// Load fuel statistics
// YEAR	QUARTER	MONTH	AIRLINE_ID	UNIQUE_CARRIER	CARRIER	CARRIER_NAME	CARRIER_GROUP_NEW	SALA_GALLONS	SDOM_GALLONS	SDOMT_GALLONS	SATL_GALLONS	SPAC_GALLONS	SLAT_GALLONS	SINT_GALLONS	TS_GALLONS	NALA_GALLONS	NDOM_GALLONS	NDOMT_GALLONS	NATL_GALLONS	NPAC_GALLONS	NLAT_GALLONS	MAC_GALLONS	NINT_GALLONS	TN_GALLONS	TDOMT_GALLONS	TINT_GALLONS	TOTAL_GALLONS	SALA_COST	SDOM_COST	SDOMT_COST	SATL_COST	SPAC_COST	SLAT_COST	SINT_COST	TS_COST	NALA_COST	NDOM_COST	NDOMT_COST	NATL_COST	NPAC_COST	NLAT_COST	MAC_COST	NINT_COST	TN_COST	TDOMT_COST	TINT_COST	TOTAL_COST
LOAD CSV WITH HEADERS FROM 'file:///15729-DIS-ex2-Dataset/Additional_Data/monthly-fuel-statistics-2014.csv'
AS row
CREATE (:Fuel {
  year:               toInteger(row.YEAR),
  quarter:            toInteger(row.QUARTER),
  month:              toInteger(row.MONTH),
  airlineID:          row.AIRLINE_ID,
  uniqueCarrier:      row.UNIQUE_CARRIER,
  carrier:            row.CARRIER,
  carrierName:        row.CARRIER_NAME,
  totalGallons:       row.TOTAL_GALLONS,
  totalCost:          row.TOTAL_COST
})




CREATE INDEX FOR (F:Flight) ON (F.uniqueCarrier);
CREATE INDEX FOR (F:Flight) ON (F.tailNum);
CREATE INDEX FOR (F:Flight) ON (F.dest);
CREATE INDEX FOR (F:Flight) ON (F.origin);
CREATE INDEX FOR (F:Flight) ON (F.cancellationCode);
CREATE INDEX FOR (A:Airport) ON (A.iata);
CREATE INDEX FOR (P:Plane) ON (P.tailNum);
CREATE INDEX FOR (C:Carrier) ON (C.code);
CREATE INDEX FOR (F:Fuel) ON (F.uniqueCarrier);




// Flight -> Plane

MATCH (F:Flight)
CALL {
WITH F
MATCH (P:Plane)
WHERE F.tailNum = P.tailnum
CREATE (F)-[r:executedWith]->(P)
} IN TRANSACTIONS OF 50000 ROWS
;


// Origin Airport -> Flight
MATCH (F:Flight)
CALL {
WITH F
MATCH (A:Airport)
  WHERE F.origin = A.iata
CREATE (F)-[r:fromAirport]->(A)
} IN TRANSACTIONS OF 50000 ROWS
;

//  Flight -> Destination Airport
MATCH (F:Flight)
CALL {
WITH F
MATCH (A:Airport)
  WHERE F.dest = A.iata
CREATE (F)-[r:to]->(P)
} IN TRANSACTIONS OF 50000 ROWS
;

// Carrier -> Flight
MATCH (F:Flight)
CALL {
WITH F
MATCH (C:Carrier)
  WHERE F.uniqueCarrier = C.code
CREATE (F)-[r:performedBy]->(C)
} IN TRANSACTIONS OF 50000 ROWS
;

// Cancellation -> Flight
MATCH (F:Flight)
CALL {
WITH F
MATCH (C:Cancellation)
  WHERE F.cancellationCode = C.code
CREATE (F)-[r:cancelledBy]->(C)
} IN TRANSACTIONS OF 50000 ROWS
;

// Carrier -> Fuel
MATCH (C:Carrier)
CALL {
WITH C
MATCH (F:Fuel)
  WHERE F.uniqueCarrier = C.code
CREATE (C)-[r:consumed]->(F)
} IN TRANSACTIONS OF 50000 ROWS
;

MATCH (A:Airport)-[r:fromAirport]->(F:Flight)
DELETE r
;