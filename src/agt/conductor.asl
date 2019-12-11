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

+!start
    <-  getASong(Name);
        +songName(Name);
        .concat("data/", Name, ".mid", File);
        !register;
        !loadSheet(File).

//Register the agent as conductor
+!register <- .df_register("conductor").

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
+!orchestrate <- !prepareToPlay.
    
+!prepareToPlay : limitTicks(L)
    <-  setMaxTick(L);
        !play.

+!prepareToPlay
    <- !play.

// Read the next notes and plays it
@p1[atomic]
+!play : hasTicks(H) & H == true
    <-  nextTick(Length, Types, Instruments, Notes, Volume);
        !sendNotes(Length, Types, Instruments, Notes, Volume); 
        !waitPlay.

+!play <- .broadcast(tell, end).

+!end <- .print("Finished playing"); .stopMAS.

// Waits for the next metronome tick
@w1[atomic]
+!waitPlay <-  waitTick; !play.

+!sendNotes(L, [T | TS], [I | IS], [N | NS], [V | VS]) : L>0
    <-  //.send(musician, achieve, play(T, I, N, V));
        //.broadcast(tell, next(T, I, N, V));
        .df_search(I,LP);
        .send(LP,tell,next(T, I, N, V));
        !sendNotes(L,TS, IS, NS, VS).
        
+!sendNotes(_, [], [], [], []).

// wait till the moment there is only the contracted musicians into the MAS
+!invite : notReady <- .all_names(L);
                         .length(L,Len);
                         .wait(500);
                         !verify(Len-1);
                         !invite.

+!verify(L) : mCount(N) & L==N 
            <- -notReady.  

+!verify(L) : mCount(N) & L<N & sheetName(NAME)
            <-  .print("There are not enough musicians for music ", NAME);
                .stopMAS.

+!verify(L).

+!invite : mCount(N) 
            <- .print("Inviting musicians for the organisation ...");
                // get artifact id of scheme "orchestra_group"
                lookupArtifact("orchestra_group", GId);      
                //change roles dependencies                  
                admCommand(setCardinality(role, musician, 0, N))[aid(GId)];
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
                .df_search(Instrument,LP);
                !startCNP(N,Instrument,LP);
                getNeededInstrument(NextIntrument, Cond);
                -mCount(N);
                +mCount(N+1);
                !cNPcycle(N+1,NextIntrument, Cond).

+!cNPcycle(N,Instrument, V) <-  .print("This music needs ",N," musicians.");
                                .broadcast(achieve, kill).
+!kill.

+!startCNP(Id,Mus,LP) : LP \==[]
   <- +cnp_state(Id,propose);                   // remember the state of the CNP
      //.print("Sending CFP to ",LP);
      .send(LP,tell,cfp(Id,Mus));
      // the deadline of the CNP is now + 4 seconds (or all proposals were received)
      .wait(all_proposals_received(Id,.length(LP)), 4000, _);
      !contract(Id,Mus).

+!startCNP(Id,Mus,LP) <- .print("No ", Mus, " available.").

// this plan needs to be atomic so as not to accept
// proposals or refusals while contracting
@lc1[atomic]
+!contract(CNPId,Mus)
   :  cnp_state(CNPId,propose)
   <- -cnp_state(CNPId,_);
      +cnp_state(CNPId,contract);
      .findall(offer(O,A),propose(CNPId,O)[source(A)],L);
      //.print("Offers are ",L);
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
