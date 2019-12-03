/* Initial beliefs and rules */
mCount(0).
notReady.

all_proposals_received(CNPId,NP) :-              // NP = number of participants
     .count(propose(CNPId,_)[source(_)], NO) &   // number of proposes received
     .count(refuse(CNPId)[source(_)], NR) &      // number of refusals received
     NP = NO + NR.

/* Desires */

/* Plans */

+!start : songName(Name) 
    <-  .concat("data/", Name, ".mid", File);
        !register;
        !loadSheet(File).

//Register the agent as maestro
+!register <- .df_register("maestro").

// Loads a sheet
+!loadSheet(Sheet) : true 
    <-  readSheet(Sheet, S);
        !checkSheet(Sheet, S).

// Check if the sheet was successfully loaded
+!checkSheet(Sheet, S) : S == false <- .print("Failed to load '", Sheet, "'").

+!checkSheet(Sheet, S) : sheetName(NAME)
    <-  .print("Sheet loaded");
        .print("Name: ", NAME).
        //!prepareToPlay.

//Initialize the music
+!coordenar <- !prepareToPlay.
    
+!prepareToPlay : limitTicks(L)
    <-  setMaxTick(L);
        !play.

+!prepareToPlay
    <- !play.

// Read the next notes and plays it
+!play : hasTicks(H) & H == true
    <-  tickNotes(Length, Types, Instruments, Notes, Volume); 
        !playNotes(Length, Types, Instruments, Notes, Volume);
        !waitPlay.

+!play <- .broadcast(tell, end).

+!finalizar <- .print("Finished playing"); .stopMAS.

//+!play(T, I, N, V). // Just to prevent errors when sendind "play" to everyone

// Waits for the next metronome tick
//+!waitPlay <-   nextTick; !play.
+!waitPlay <-  waitTick; nextTick; !play.

// Tell the musicians what to do
+!playNotes(L, T, I, N, V) : L > 0
    <-  //.print(T, I, N, V); 
        !sendNotes(T, I, N, V).

+!playNotes(L, T, I, N, V).

+!sendNotes([T | TS], [I | IS], [N | NS], [V | VS])
    <-  //.send(musician, achieve, play(T, I, N, V));
        // .broadcast(achieve, play(T, I, N, V));
        .broadcast(tell, next(T, I, N, V));
        !sendNotes(TS, IS, NS, VS).
        
+!sendNotes([], [], [], []).

// wait till the moment there is only the contracted musicians into the MAS
+!solicita : notReady <- .all_names(L);
                         .length(L,Len);
                         !verify(Len-1);
                         !solicita.

+!verify(L) : mCount(N) & L==N 
            <- -notReady.

+!verify(L).

+!solicita : mCount(N) 
            <- .print("Solicitando entrada de musicos na organizacao...");
                // get artifact id of scheme "orchestra_group"
                lookupArtifact("orchestra_group", GId);      
                //change roles dependencies                  
                admCommand(setCardinality(role, musician, 1, N))[aid(GId)];
                // get artifact id of scheme "orchestra_inst"
                lookupArtifact("orchestra_inst", SId);     
                //changes quantity of mussician needed                   
                admCommand(setCardinality(mission, mMusician, N, N))[aid(SId)];
                .broadcast(achieve,enterOrg).

+!enterOrg.

// ---------------------------------------- CNP ------------------------------------------------

// start the CNP
+!cnp <- .print("Waiting musicians...");
         .wait(2000);
         getNeededInstrument(Instrument, V);
         !cNPcycle(0,Instrument, V).

+!cNPcycle(N,Instrument, V) : V == true
            <- .print("Contracting musician for: |", Instrument,"|");
                !startCNP(N,Instrument);
                getNeededInstrument(NextIntrument, Cond);
                -mCount(N);
                +mCount(N+1);
                !cNPcycle(N+1,NextIntrument, Cond).

+!cNPcycle(N,Instrument, V) <- .broadcast(achieve, kill).
+!kill.

+!startCNP(Id,Mus)
   <- +cnp_state(Id,propose);                   // remember the state of the CNP
      .df_search(Mus,LP);
      .print("Sending CFP to ",LP);
      .send(LP,tell,cfp(Id,Mus));
      // the deadline of the CNP is now + 4 seconds (or all proposals were received)
      .wait(all_proposals_received(Id,.length(LP)), 4000, _);
      !contract(Id).

// this plan needs to be atomic so as not to accept
// proposals or refusals while contracting
@lc1[atomic]
+!contract(CNPId)
   :  cnp_state(CNPId,propose)
   <- -cnp_state(CNPId,_);
      +cnp_state(CNPId,contract);
      .findall(offer(O,A),propose(CNPId,O)[source(A)],L);
      .print("Offers are ",L);
      L \== [];                                 // constraint the plan execution to at least one offer
      .min(L,offer(WOf,WAg));                   // sort offers, the first is the best
      .print("Winner is ",WAg," with ",WOf);
      !announce_result(CNPId,L,WAg);
      -cnp_state(CNPId,_);
      .abolish(propose(CNPId,_)).

// nothing to do, the current phase is not 'propose'
@lc2 +!contract(_).

-!contract(CNPId)
   <- .print("CNP ",CNPId," has failed!").

+!announce_result(_,[],_).

// announce to the winner
+!announce_result(CNPId,[offer(_,WAg)|T],WAg)
   <- .send(WAg,tell,accept_proposal(CNPId));
      !announce_result(CNPId,T,WAg).

// announce to others
+!announce_result(CNPId,[offer(_,LAg)|T],WAg)
   <- .send(LAg,tell,reject_proposal(CNPId));
      !announce_result(CNPId,T,WAg).


// jacamo includes
{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }
