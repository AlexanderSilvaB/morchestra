!start.

+!start : true <- !loadSheet("data/jurassic_park.mid").

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

// Tell the musicians what to do (Remove playTick and use Types, Channels, Notes and Volume)
+!play : hasTicks(H) & H == true
    <-  playTick;
        //tickNotes(Types, Channels, Notes, Volume); 
        //.print(Notes);
        !waitPlay.

+!play <- .print("Finished playing").

// Waits for the next metronome tick
//+!waitPlay <-   nextTick; !play.
+!waitPlay <-  waitTick; nextTick; !play.

// jacamo includes
{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$moiseJar/asl/org-obedient.asl") }
