import java.rmi.*;
import java.util.*;

public class Client {
    public static void main(String[] args) throws Exception {
        Init init = (Init) Naming.lookup("rmi://localhost:2150/Fat32Reader");
		CurrentDir current = (CurrentDir) Naming.lookup("rmi://localhost:2150/Fat32Reader");
        init.initiate(args[0]);
		Scanner sc = new Scanner(System.in);
		while (true){
			System.out.print(current.getCurrentDir() +"] ");
			String input = sc.nextLine();
			String[] arg = input.split(" ");
			switch (arg[0]) {
				case "info":
				    // System.out.println("info");
                    Info info = (Info) Naming.lookup("rmi://localhost:2150/Fat32Reader");
                    String stuff = info.info();
                    System.out.println(stuff);
					break;
				case "ls":
					LS ls = (LS) Naming.lookup("rmi://localhost:2150/Fat32Reader");
					String lsResult = "";
					if(arg.length == 1) lsResult = ls.ls(".");
					else lsResult = ls.ls(arg[1].toUpperCase());
					System.out.println(lsResult);
					break;
				case "stat":
					Stat stat = (Stat) Naming.lookup("rmi://localhost:2150/Fat32Reader");
					String statResult = "";
					if(arg.length == 1) statResult = stat.stat(".");
					else statResult = stat.stat(arg[1].toUpperCase());
					System.out.println(statResult);
					break;
				case "cd":
					CD cd = (CD) Naming.lookup("rmi://localhost:2150/Fat32Reader");
					String cdResult = "";
					cdResult = cd.cd(arg[1].toUpperCase());
					if(cdResult.equals("")){
						//dont do anything
					}
					else System.out.println(cdResult);
					break;
				case "open":
					Open open = (Open) Naming.lookup("rmi://localhost:2150/Fat32Reader");
					String openResult = "";
					openResult = open.open(arg[1].toUpperCase());
					System.out.println(openResult);
					break;
				case "close":
					Close close = (Close) Naming.lookup("rmi://localhost:2150/Fat32Reader");
					String closeResult = "";
					closeResult = close.close(arg[1].toUpperCase());
					System.out.println(closeResult);
					break;
				case "read":
					Read read = (Read) Naming.lookup("rmi://localhost:2150/Fat32Reader");
					String readResult = "";
					readResult = read.read(arg[1].toUpperCase(), Integer.parseInt(arg[2]), Integer.parseInt(arg[3]));
					System.out.println(readResult);
					break;
				case "size":
					Size size = (Size) Naming.lookup("rmi://localhost:2150/Fat32Reader");
					String sizeResult = "";
					sizeResult = size.size(arg[1].toUpperCase());
					System.out.println(sizeResult);
					break;
				case "quit":
					System.exit(0);
				default:
					System.out.println("Error: Invalid Argument");
            }
        }
    }
}
