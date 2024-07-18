
// q2.a.1
MATCH (F:Flight)
RETURN labels(F), F.year, count(*)
;

// q2.a.2
MATCH (F:Flight)-[r:executedWith]-(P:Plane)
RETURN F.year, COUNT(DISTINCT P.tailnum)