import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class Client {
    public static void main(String[] args) throws Exception {
        // init.initiate(args[0]);
		Registry registry = null;
		try{
			registry = LocateRegistry.getRegistry(args[0], Integer.parseInt(args[1]));
		} catch (Exception e){
			System.out.println("Error: hostName or Port not found");
			System.exit(1);
		}
		Scanner sc = new Scanner(System.in);
		// Impl impl = new Impl();
		// Init init = (Init) registry.lookup("Fat32Reader");
		CurrentDir current = (CurrentDir) registry.lookup("Fat32Reader");
		while (true){
			System.out.print(current.getCurrentDir() +"] ");
			String input = sc.nextLine();
			String[] arg = input.split(" ");
			switch (arg[0]) {
				case "info":
                    Info info = (Info) registry.lookup("Fat32Reader");
                    String infoResult = info.info();
                    System.out.println(infoResult);
					break;
				case "ls":
					LS ls = (LS) registry.lookup("Fat32Reader");
					String lsResult = "";
					if(arg.length == 1) lsResult = ls.ls(".");
					else lsResult = ls.ls(arg[1].toUpperCase());
					System.out.println(lsResult);
					break;
				case "stat":
					Stat stat = (Stat) registry.lookup("Fat32Reader");
					String statResult = "";
					if(arg.length == 1) statResult = stat.stat(".");
					else statResult = stat.stat(arg[1].toUpperCase());
					System.out.println(statResult);
					break;
				case "cd":
					CD cd = (CD) registry.lookup("Fat32Reader");
					String cdResult = cd.cd(arg[1].toUpperCase());
					if(cdResult.equals("")){
						//dont do anything
					}
					else System.out.println(cdResult);
					break;
				case "open":
					Open open = (Open) registry.lookup("Fat32Reader");
					String openResult = open.open(arg[1].toUpperCase());
					System.out.println(openResult);
					break;
				case "close":
					Close close = (Close) registry.lookup("Fat32Reader");
					String closeResult = close.close(arg[1].toUpperCase());
					System.out.println(closeResult);
					break;
				case "read":
					Read read = (Read) registry.lookup("Fat32Reader");
					String readResult = read.read(arg[1].toUpperCase(), Integer.parseInt(arg[2]), Integer.parseInt(arg[3]));
					System.out.println(readResult);
					break;
				case "size":
					Size size = (Size) registry.lookup("Fat32Reader");
					String sizeResult = size.size(arg[1].toUpperCase());
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
