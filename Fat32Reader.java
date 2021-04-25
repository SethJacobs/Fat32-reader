import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Fat32Reader
 */
public class Fat32Reader {

	int BPB_BytsPerSec, BPB_SecPerClus, BPB_RsvdSecCnt, BPB_NumFATs, BPB_FATSz32, BPB_RootClus;
	int BPB_RootEntCnt, RootDirSectors, FirstDataSector, FATOffSet, FatSecNum, FATEntOffset;
	int FirstSectorofCluster, FatTableStart, bytesPerCluster, clustInFat;
	int currentDIR, root;
	byte[] data;
	ArrayList<String> lsList = new ArrayList<>();
	HashMap<Integer,Integer> parentMap = new HashMap<>();
	LinkedList<String> cdList = new LinkedList<>();
	HashSet<String> openList = new HashSet<>();

	public static void main(String[] args) throws IOException {
		Fat32Reader fr = new Fat32Reader();
		fr.initiate(args[0]);
		Scanner sc = new Scanner(System.in);
		while (true){
			System.out.print(fr.getCurrentDir() +"] ");
			String input = sc.nextLine();
			String[] arg = input.split(" ");
			switch (arg[0]) {
				case "info":
					fr.info();
					break;
				case "ls":
					if(arg.length == 1) fr.ls(".");
					else fr.ls(arg[1].toUpperCase());
					break;
				case "stat":
					if(arg.length == 1) fr.stat(".");
					else fr.stat(arg[1].toUpperCase());
					break;
				case "cd":
					// if(arg.length == 1) fr.ls(".");
					fr.cd(arg[1].toUpperCase());
					break;
				case "open":
					fr.open(arg[1].toUpperCase());
					break;
				case "close":
					fr.close(arg[1].toUpperCase());
					break;
				case "read":
					fr.read(arg[1].toUpperCase(), Integer.parseInt(arg[2]), Integer.parseInt(arg[3]));
					break;
				case "size":
					fr.size(arg[1].toUpperCase());
					break;
				default:
					System.out.println("Error: Invalid Argument");
			}
		}
	}

	public void initiate(String paths) throws IOException {
		ArrayList<Integer> list = new ArrayList<>();
		Path path = Paths.get(paths);
		data = Files.readAllBytes(path);
		BPB_BytsPerSec = getBytes(11,2);
		BPB_RsvdSecCnt = getBytes(14,2);
		BPB_SecPerClus = getBytes(13, 1);
		BPB_NumFATs = getBytes(16,1);
		BPB_FATSz32 = getBytes(36,4);
		BPB_RootClus = getBytes(44,4);
		BPB_RootEntCnt = getBytes(17,2);
		RootDirSectors = ((BPB_RootEntCnt * 32) + (BPB_BytsPerSec - 1)) / BPB_BytsPerSec;
		FirstDataSector = BPB_RsvdSecCnt + (BPB_NumFATs * BPB_FATSz32) + RootDirSectors;
		FirstSectorofCluster = ((BPB_RootClus - 2) * BPB_SecPerClus) + FirstDataSector;
		FatTableStart = FatSecNum * BPB_BytsPerSec;
		bytesPerCluster = BPB_BytsPerSec * BPB_SecPerClus;
		root = getDir(list, BPB_RootClus);
		currentDIR = root;
	}

	public void info() {
		System.out.println("BPB_BytsPerSec: 0x" + Integer.toHexString(BPB_BytsPerSec) + ", " + BPB_BytsPerSec);
		System.out.println("BPB_SecPerClus: 0x" + Integer.toHexString(BPB_SecPerClus) + ", " + BPB_SecPerClus);
		System.out.println("BPB_RsvdSecCnt: 0x" + Integer.toHexString(BPB_RsvdSecCnt) + ", " + BPB_RsvdSecCnt);
		System.out.println("BPB_NumFATs: 0x" + Integer.toHexString(BPB_NumFATs) + ", " + BPB_NumFATs);
		System.out.println("BPB_FATSz32: 0x" + Integer.toHexString(BPB_FATSz32) + ", " + BPB_FATSz32);
	}

	public void ls(int dir){
		if (getBytes(dir+11, 1) == 32){
			System.out.println("Error: Not a Directory");
			return;
		}
		ArrayList<String> files = new ArrayList<>();
		if (dir == root){
			for (int i = root;  i < root + bytesPerCluster; i += 64)  {
				String dirName = getStringFromBytes(i, 11);
				dirName = nameNice(dirName).trim();
				if (i != root && !dirName.contains("~1"))
					files.add(dirName);
			}
		}
		ArrayList<Integer> dirStarts = new ArrayList<Integer>();
		String low = Integer.toHexString(getBytes(dir + 26, 2));
		String hi = Integer.toHexString(getBytes(dir + 20, 2));
		int firstclust = Integer.parseInt(hi + low, 16);
		// clustInFat = getBytes(i+26, 2);
		getDir(dirStarts, firstclust);
		for (Integer integer : dirStarts) {
			for (int j = integer + 32; j < integer + bytesPerCluster; j+= 64) {
				String currentName = getStringFromBytes(j, 11);
				if (currentName.contains("\u0000")) continue;
				currentName = nameNice(currentName).trim();
				files.add(currentName);
			}
		}
		Collections.sort(files);
		System.out.print(". ");
		for (String file : files) {
			System.out.print(file + " ");
		}
		System.out.println(" ");
	}

	public void stat(int dir) {
		
		System.out.println("Size is " + getBytes(dir+28, 4));

		int attr = getBytes(dir+11, 1);
		ArrayList<String> attributes = new ArrayList<>();
		if((attr & 0x20) == 0x20) attributes.add("ATTR_ARCHIVE");
		if((attr & 0x10) == 0x10) attributes.add("ATTR_DIRECTORY");
		if((attr & 0x08) == 0x08) attributes.add("ATTR_VOLUME_ID");
		if((attr & 0x04) == 0x04) attributes.add("ATTR_SYSTEM");
		if((attr & 0x02) == 0x02) attributes.add("ATTR_HIDDEN");
		if((attr & 0x01) == 0x01) attributes.add("ATTR_READ_ONLY");
		System.out.print("Attributes ");
		for (String string : attributes) {
			System.out.print(string + " ");
		}

		String low = Integer.toHexString(getBytes(dir + 26, 2));
		String hi = Integer.toHexString(getBytes(dir + 20, 2));
		int firstclust = Integer.parseInt(hi + low, 16);
		System.out.println("Next cluster is " + Integer.toHexString(firstclust).toUpperCase());
		
		// currentDIR = root;
		
	}

	public void ls(String dirName){
		StringTokenizer st = new StringTokenizer(dirName, File.separator);
		switch (dirName) {
			case ".":
				ls(currentDIR);
				break;
			case "..":
				if(currentDIR != root){
					goToDir(currentDIR, st, dirName, "ls");
				} else System.out.println("Error: No Directory Found");
				
				break;
			
			default:
				goToDir(currentDIR, st, dirName, "ls");
				break;
		}
	}

	public void stat(String dirName){
		StringTokenizer st = new StringTokenizer(dirName, File.separator);
		switch (dirName) {
			case ".":
				stat(currentDIR);
				break;
			case "..":
				if(currentDIR != root){
					goToDir(currentDIR, st, dirName, "stat");
				} else System.out.println("Error: No Directory Found");
				
				break;
			
			default:
				goToDir(currentDIR, st, dirName, "stat");
				break;
		}
	}

	public void cd(String dirName) {
		switch (dirName) {
			case "..":
				if(currentDIR != root){
					StringTokenizer st = new StringTokenizer(dirName, File.separator);
					goToDir(currentDIR, st, dirName, "cd");
				} else System.out.println("Error: No Directory Found");
				
				break;
			
			default:
				StringTokenizer st = new StringTokenizer(dirName, File.separator);
				goToDir(currentDIR, st, dirName, "cd");
				break;
		}
	}

	//marks a file as open if it exists by putting it in the open list
	public void open(String name) {
		StringTokenizer st = new StringTokenizer(name, File.separator);
		String fullPath  = getCurrentDir() + File.separator + name;
		if(goToDir(currentDIR, st, name, "open")){
			if (!openList.contains(fullPath)){
				openList.add(fullPath);
				System.out.println(name + " is open");
			} else {
				System.out.println(name + " is already open");
			}
		} else {
			System.out.println("Error: " + fullPath + " is not a file");
		}
	}

	//Marks a file as closed only if it exists and is in the open list
	public void close(String name) {
		StringTokenizer st = new StringTokenizer(name, File.separator);
		String fullPath  = getCurrentDir() + File.separator + name;
		if(goToDir(currentDIR, st, name, "close")){
			if (openList.contains(fullPath)){
				openList.remove(fullPath);
				System.out.println(name + " is closed");
			} else {
				System.out.println(name + " is already closed");
			}
		} else {
			System.out.println("Error: " + fullPath + " is not a file");
		}
	}

	public void size(String dirName) {
		StringTokenizer st = new StringTokenizer(dirName, File.separator);
		switch (dirName) {
			case ".":
				size(currentDIR, dirName);
				break;
			case "..":
				if(currentDIR != root){
					if(!goToDir(currentDIR, st, dirName, "size")){
						System.out.println("Error: " + dirName + " is not a file");
					}
				} else System.out.println("Error: No Directory Found");
				
				break;
			
			default:
				if(!goToDir(currentDIR, st, dirName, "size")){
					System.out.println("Error: " + dirName + " is not a file");
				}
				break;
		}
	}

	public void size(int dir, String path) {
		System.out.println("Size of " + path + " is " + getBytes(dir+28, 4) + " bytes");
	}

	//Reads text from a file
	public void read(String path, int offset, int numOfBytes) {

		//ERRORS
		if (offset < 0){ 
			System.out.println("Error: OFFSET must be a positive value");
			return;
		}
		if (numOfBytes <= 0){
			System.out.println("Error: NUM_BYTES must be a positive value");
			return;
		}

		StringTokenizer st = new StringTokenizer(path, File.separator);
		String fullPath  = getCurrentDir() + File.separator + path;
		if (openList.contains(fullPath)){
			
		} else {
			System.out.println("Error: file is not open");
		}
	}

	//most important method. goes to the directory where we want it to go
	public boolean goToDir(int dir, StringTokenizer st, String fullPath, String command) {
		// boolean error = false;
		int dirTrain = currentDIR;
		boolean found = false;
		while(st.hasMoreTokens()){
			found = false;
			String name = st.nextToken();
			if(name.equals("..")){
				if (parentMap.get(dirTrain) == null) {
					found = false;
					System.out.println("Error: No Directory Found");
				} else{
					if (command.equals("cd")) cdList.removeLast();
					found = true;
					dirTrain = parentMap.get(dirTrain);
				}
			}
			else {
				ArrayList<Integer> dirStarts = new ArrayList<Integer>();
				String low = Integer.toHexString(getBytes(dirTrain+ 26, 2));
				String hi = Integer.toHexString(getBytes(dirTrain + 20, 2));
				int firstclust = Integer.parseInt(hi + low, 16);
				// clustInFat = getBytes(i+26, 2);
				if (dirTrain == root) getDir(dirStarts, BPB_RootClus);
				else getDir(dirStarts, firstclust);

				for (Integer integer : dirStarts) {
					for (int j = integer+32; j < integer + bytesPerCluster; j+= 64) {
						if ((j-32) == root) j -= 32;
						int attr = getBytes(j+11, 1);
						parentMap.put(j, dirTrain);
						String currentName = getStringFromBytes(j, 11);
						currentName = nameNice(currentName).trim();
						// System.out.println(currentName + counter++);
						if (command.equals("stat") || command.equals("open") || command.equals("ls") || command.equals("close") || command.equals("size")) {
							if ((attr & 0x10) == 0x10 && (command.equals("open") || command.equals("close") || command.equals("size")) && !currentName.equals("..")){
								return false;
							}
							if (currentName.equals(name)) {
								found = true;
								dirTrain = j;
								break;
							}
						} else if (command.equals("cd")) {
							if ((attr & 0x10) == 0x10 && (attr & 0x02) != 0x02){
								if (currentName.equals(name)) {
									cdList.addLast(name);
									found = true;
									dirTrain = j;
									break;
								}
							}
						}
					}
				}
			}
		}
		if (found == false){
			// error = true;
			System.out.println("Error: " + fullPath + " is not a directory");
			return false;
		} 
		else if (command.equals("ls")) {
			ls(dirTrain);
		} 
		else if (command.equals("stat")) {
			stat(dirTrain);
		}
		else if (command.equals("cd")) {
			currentDIR = dirTrain;
		} else if (command.equals("size")) {
			size(dirTrain, fullPath);
		}
		return true;
	}

	public int getBytes(int offset, int size) {
		String hex = "";
		for(int i = offset + size - 1; i >= offset; i--){
			String temp = Integer.toHexString(data[i] & 0xFF);
			if(Integer.parseInt(temp, 16) < 16) {
				hex += "0" + temp;
			} else hex += temp;
		}
		int result = Integer.parseInt(hex, 16);
		return result;
	}

	public int getDir(ArrayList<Integer> list, int start) {
		int n = start; 
		FATOffSet = n * 4;
		FatSecNum = BPB_RsvdSecCnt + (FATOffSet / BPB_BytsPerSec);
		FatTableStart = FatSecNum * BPB_BytsPerSec;
		FATEntOffset = FATOffSet % BPB_BytsPerSec;
		int clusterOffset = FATEntOffset + FatTableStart;
		int nextClus = getBytes(clusterOffset, 4);
        int firstSectorofDirCluster = ((n - 2) * BPB_SecPerClus) + FirstDataSector;
        int startOfDir = firstSectorofDirCluster * BPB_BytsPerSec;
		// bytesPerCluster = BPB_BytsPerSec * BPB_SecPerClus;
		list.add(startOfDir);
		if(nextClus <= 268435447) {
            getDir(list, nextClus); //recursively search the next cluster
        }
		return startOfDir;
	}

	public String getStringFromBytes(int offset, int size) {
        byte[] newData = new byte[size];
        int j = size - 1;
        for(int i = offset + size - 1; i >= offset; i--){
            newData[j] = data[i];
            j--;
        }
        String s = new String(newData);
        if(newData[0] == -27){
           char[] charArray = s.toCharArray();
           charArray[0] = (char)229;
           s = String.valueOf(charArray);
        }
        return s;
    }

	public String getCurrentDir() {
		if(currentDIR == root) return File.separator;
		StringBuilder sb = new StringBuilder();
		for (String string : cdList) {
			sb.append(File.separator + string);
		}
		return sb.toString();
	}

	public String nameNice(String dir) {
		if(dir.endsWith("   ")){
			dir.replaceAll(" ", "");
			return dir;
		}
		dir = dir.replaceAll(" ", "");
		StringBuilder sb = new StringBuilder(dir);
		sb.insert(dir.length()-3, ".");
		return sb.toString();
	}
	
}