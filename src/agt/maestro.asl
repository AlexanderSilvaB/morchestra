!start.

+!start : true <- !loadSheet("data/ghostbusters.mid").

// Loads a sheet
+!loadSheet(Sheet) : true 
    <-  readSheet(Sheet, S);
        !checkSheet(Sheet, S).

// Check if the sheet was successfully loaded
+!checkSheet(Sheet, S) : S == false <- .print("Failed to load '", Sheet, "'").

+!checkSheet(Sheet, S) : sheetName(NAME)
    <-  .print("Sheet loaded");
        .print("Name: ", NAME);
        !play.

// Read the next notes and plays it
+!play : hasTicks(H) & H == true
    <-  tickNotes(Length, Types, Instruments, Notes, Volume); 
        !playNotes(Length, Types, Instruments, Notes, Volume);
        !waitPlay.

+!play <- .print("Finished playing"); .stopMAS.

// Waits for the next metronome tick
//+!waitPlay <-   nextTick; !play.
+!waitPlay <-  waitTick; nextTick; !play.

// Tell the musicians what to do
+!playNotes(L, T, I, N, V) : L > 0
    <-  .print(T, I, N, V); 
        !sendNotes(T, I, N, V).

+!playNotes(L, T, I, N, V).

+!sendNotes([T | TS], [I | IS], [N | NS], [V | VS])
    <-  .send(musician, achieve, play(T, I, N, V));
        !sendNotes(TS, IS, NS, VS).
        
+!sendNotes([], [], [], []).

// jacamo includes
{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }
