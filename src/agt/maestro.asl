!start.

+!start : true 
    <-  readSheet("data/Rickandmortytg.mid", S);
        .print("Load sheet: ", S).

// jacamo includes
{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }
