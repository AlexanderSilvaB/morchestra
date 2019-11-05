!start.

+!start : true <- .print("Ready to play").

+!play(T, I, N, V)
    <-  .print([T, I, N, V]); 
        play(T, I, N, V).

// jacamo includes
{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }
