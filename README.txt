Names: Elie Benzaquen, Binyamin Jachter, Seth Jacobs
Fat32Reader.java: runs an infinite loop to intake commands to read data from the IMG file given as an initial argument
To compile: First, make sure the Server and Client and both compiled as well as compiling the Impl class using rmic Impl. Make sure that Client and Server are all pointing to your current that the server is running on.
To run: Start an rmiregistry in the command line. Then run the server with the fat32.img file as an argument. Then on another computer run Client with the IP and Port as command line arguments. 
Stubs: LS, CD, Open, Close, Info, Read, Size, Stat, CurrentDir.
Classes: Server, Client, Impl
Server - Creates a server to run the Fat32Reader when a stub method is sent to it.
Client - Sends stub methods to the server so the client does not have to run the Fat32Reader on their own machie.
Impl - Acts as the Fat32Reader and implements all the stub classes.
