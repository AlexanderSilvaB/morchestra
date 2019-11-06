!start.

+!start : myInstrument(S)
    <- !instrumentSelected(S).

+!start <- 
    pickAnInstrument(S); 
    !instrumentSelected(S).

+!instrumentSelected(S) 
    <- .print("I play ", S);
        -myInstrument(_);
        +myInstrument(S).

+!play(T, I, N, V) : myInstrument(I)
    <-  .print([T, I, N, V]); 
        play(T, I, N, V).

+!play(T, I, N, V).

// jacamo includes
{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }
