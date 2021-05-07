import java.rmi.*;
import java.util.*;

public class Client {
    public static void main(String[] args) throws Exception {
        Fat32Reader fr = new Fat32Reader();
        Init init = (Init) Naming.lookup("rmi://localhost:2150/Fat32Reader");
        init.initiate(args[0]);
		Scanner sc = new Scanner(System.in);
		while (true){
			System.out.print(fr.getCurrentDir() +"] ");
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
					System.out.println("ls");
					// if(arg.length == 1) fr.ls(".");
					// else fr.ls(arg[1].toUpperCase());
					break;
				case "stat":
					System.out.println("stat");
					// if(arg.length == 1) fr.stat(".");
					// else fr.stat(arg[1].toUpperCase());
					break;
				case "cd":
				System.out.println("cd");
					// fr.cd(arg[1].toUpperCase());
					break;
				case "open":
				System.out.println("open");
					// fr.open(arg[1].toUpperCase());
					break;
				case "close":
				System.out.println("close");
					// fr.close(arg[1].toUpperCase());
					break;
				case "read":
				System.out.println("read");
					// fr.read(arg[1].toUpperCase(), Integer.parseInt(arg[2]), Integer.parseInt(arg[3]));
					break;
				case "size":
				System.out.println("size");
					// fr.size(arg[1].toUpperCase());
					break;
				default:
					System.out.println("Error: Invalid Argument");
            }
        }
    }
}
