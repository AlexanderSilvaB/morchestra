mustDie.

!start.

+!start : myInstrument(S)
    <- !instrumentSelected(S).

+!start <- 
    pickAnInstrument(S); 
    !instrumentSelected(S).

+!instrumentSelected(S) : .random(R) & V = math.round(50 + (1000 - 50)*R)  
    <- .print("I play ", S);
        -myInstrument(_);
        +myInstrument(S);
        +price(V);
        !register.

//Register the musician in the Yellow page
+!register : myInstrument(S) 
            <- .df_register(S);
               .df_subscribe("maestro").

/*
+!play(T, I, N, V) : myInstrument(I)
    <-  .print([T, I, N, V]); 
        play(T, I, N, V).

+!play(T, I, N, V).
*/

// Musician is obligated to 'tocar'
+!tocar : next(T, I, N, V) & myInstrument(I)
         <- .print([T, I, N, V]); 
            play(T, I, N, V);
            .abolish(next(T, I, N, V));
            !tocar.

+!tocar : next(T, I, N, V)
        <-  .abolish(next(T, I, N, V));
            !tocar.

+!tocar : not end <- .wait(1);
                     !tocar.
+!tocar.

+!kill: mustDie & .my_name(N) <- //.print("I kill myself!");
                                  .kill_agent(N).

+!kill.

+!enterOrg : mustDie.

@eo[atomic]
+!enterOrg  <- //.print("Adopting musician role...");
               joinWorkspace("orchestraOrg",Org);
               lookupArtifact("orchestra_group", GId);
               focus(GId);
               adoptRole(musician);
               commitMission(mMusician).

+!entrar <- .print("I am in the orchestra.").

// ---------------------------------------- CNP ------------------------------------------------
// answer to Call For Proposal
@c1 +cfp(CNPId,Mus)[source(A)]
   :  provider(A,"maestro") & 
      myInstrument(Mus) & price(Offer)
   <- +proposal(CNPId,Mus,Offer);            // remember my proposal
      .send(A,tell,propose(CNPId,Offer)).

@r1 +accept_proposal(CNPId)
   :  proposal(CNPId,Mus,Offer)
   <- //.print("I will be the ",Mus," in the orchestra!");
      -mustDie.

@r2 +reject_proposal(CNPId)
   <- //.print("I lost CNP ",CNPId, ".");
      -proposal(CNPId,_,_).                 // clear memory
    
// jacamo includes
{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }
