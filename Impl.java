import java.io.*;
import java.nio.file.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;

public class Impl extends UnicastRemoteObject implements CurrentDir, LS, Info, CD, Stat, Size, Open, Close, Read {

    int BPB_BytsPerSec, BPB_SecPerClus, BPB_RsvdSecCnt, BPB_NumFATs, BPB_FATSz32, BPB_RootClus;
	int BPB_RootEntCnt, RootDirSectors, FirstDataSector, FATOffSet, FatSecNum, FATEntOffset;
	int FirstSectorofCluster, FatTableStart, bytesPerCluster, clustInFat;
	int currentDIR, root, OFFSET, NUM_BYTES;
	byte[] data;
	ArrayList<String> lsList = new ArrayList<>();
	HashMap<Integer,Integer> parentMap = new HashMap<>();
	LinkedList<String> cdList = new LinkedList<>();
	HashSet<String> openList = new HashSet<>();

    protected Impl() throws RemoteException {
        super();
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
	
	@Override
	public String info() throws RemoteException {
		return "BPB_BytsPerSec: 0x" + Integer.toHexString(BPB_BytsPerSec) + ", " + BPB_BytsPerSec + 
		"\nBPB_SecPerClus: 0x" + Integer.toHexString(BPB_SecPerClus) + ", " + BPB_SecPerClus + 
		"\nBPB_RsvdSecCnt: 0x" + Integer.toHexString(BPB_RsvdSecCnt) + ", " + BPB_RsvdSecCnt +
		"\nBPB_NumFATs: 0x" + Integer.toHexString(BPB_NumFATs) + ", " + BPB_NumFATs +
		"\nBPB_FATSz32: 0x" + Integer.toHexString(BPB_FATSz32) + ", " + BPB_FATSz32;
	}

	@Override
	public String ls(String dirName) throws RemoteException {
		StringTokenizer st = new StringTokenizer(dirName, File.separator);
		switch (dirName) {
			case ".":
				return ls(currentDIR);
			default:
				return goToDir(currentDIR, st, dirName, "ls");
		}
	}

	public String ls(int dir){
		if (getBytes(dir+11, 1) == 32){
			return "Error: Not a Directory";
		}
		StringBuilder sb = new StringBuilder();
		ArrayList<String> files = new ArrayList<>();
		if (dir == root){
			for (int i = root;  i < root + bytesPerCluster; i += 64)  {
				String dirName = getStringFromBytes(i, 11);
				dirName = nameNice(dirName).trim();
				if (i != root && (getBytes(i+11, 1) & 0x02) != 0x02)
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
		sb.append(". ");
		for (String file : files) {
			sb.append(file + " ");
		}
		sb.append(" ");
		return sb.toString();
	}

	@Override
    public String stat(String dirName) throws RemoteException {
        StringTokenizer st = new StringTokenizer(dirName, File.separator);
		switch (dirName) {
			case ".":
				return stat(currentDIR);
			default:
				return goToDir(currentDIR, st, dirName, "stat");
		}
    }

	public String stat(int dir) {

		StringBuilder sb = new StringBuilder();
		
		sb.append("Size is " + getBytes(dir+28, 4));

		int attr = getBytes(dir+11, 1);
		ArrayList<String> attributes = new ArrayList<>();
		if((attr & 0x20) == 0x20) attributes.add("ATTR_ARCHIVE");
		if((attr & 0x10) == 0x10) attributes.add("ATTR_DIRECTORY");
		if((attr & 0x08) == 0x08) attributes.add("ATTR_VOLUME_ID");
		if((attr & 0x04) == 0x04) attributes.add("ATTR_SYSTEM");
		if((attr & 0x02) == 0x02) attributes.add("ATTR_HIDDEN");
		if((attr & 0x01) == 0x01) attributes.add("ATTR_READ_ONLY");
		sb.append("\nAttributes ");
		for (String string : attributes) {
			sb.append(string + " ");
		}

		String low = Integer.toHexString(getBytes(dir + 26, 2));
		String hi = Integer.toHexString(getBytes(dir + 20, 2));
		int firstclust = Integer.parseInt(hi + low, 16);
		sb.append("\nNext cluster is " + Integer.toHexString(firstclust).toUpperCase());
		return sb.toString();
	}

	@Override
    public String cd(String dirName) throws RemoteException {
        StringTokenizer st = new StringTokenizer(dirName, File.separator);
		return goToDir(currentDIR, st, dirName, "cd");	
    }

	@Override
    public String open(String name) throws RemoteException {
        StringTokenizer st = new StringTokenizer(name, File.separator);
		String fullPath = "";
		if(getCurrentDir().equals(File.separator)) fullPath = File.separator + name;
		else fullPath  = getCurrentDir() + File.separator + name;
		if(!goToDir(currentDIR, st, name, "open").equals("false")){
			if (!openList.contains(fullPath)){
				openList.add(fullPath);
				return name + " is open";
			} else {
				return name + " is already open";
			}
		} else {
			return "Error: " + fullPath + " is not a file";
		}
    }

	@Override
    public String close(String name) throws RemoteException {
        StringTokenizer st = new StringTokenizer(name, File.separator);
		String fullPath  = "";
		if(getCurrentDir().equals(File.separator)) fullPath = File.separator + name;
		else fullPath  = getCurrentDir() + File.separator + name;
		if(!goToDir(currentDIR, st, name, "close").equals("false")){
			if (openList.contains(fullPath)){
				openList.remove(fullPath);
				return name + " is closed";
			} else {
				return name + " is already closed";
			}
		} else {
			return "Error: " + fullPath + " is not a file";
		}
    }

	@Override
	public String size(String dirName) throws RemoteException {
		StringTokenizer st = new StringTokenizer(dirName, File.separator);
		String answer = goToDir(currentDIR, st, dirName, "size");
		if(answer.equals("false")){
			return "Error: " + dirName + " is not a file";
		}
		return answer;
	}

	public int size(int dir) {
		return getBytes(dir+28, 4);
	}

	@Override
    public String read(String path, int offset, int numOfBytes) throws RemoteException {
        //ERRORS
		if (offset < 0){ 
			return "Error: OFFSET must be a positive value";
		}
		if (numOfBytes <= 0){
			return "Error: NUM_BYTES must be a positive value";
		}

		StringTokenizer st = new StringTokenizer(path, File.separator);
		String fullPath  = getCurrentDir() + path;
		if (openList.contains(fullPath)){
			OFFSET = offset;
			NUM_BYTES = numOfBytes;
			return goToDir(currentDIR, st, fullPath, "read");
		} else {
			return "Error: file is not open";
		}

    }

	private String fileReader(int dirTrain) {
		ArrayList<Integer> dirStarts = new ArrayList<Integer>();
		String low = Integer.toHexString(getBytes(dirTrain + 26, 2));
		String hi = Integer.toHexString(getBytes(dirTrain + 20, 2));
		int firstclust = Integer.parseInt(hi + low, 16);
		getDir(dirStarts, firstclust);
		StringBuilder sb = new StringBuilder();
		int cluster = 0;

		for (int i = 0; i < dirStarts.size()-1; i++) {
			cluster = (i+1) * bytesPerCluster;
			if (OFFSET < cluster && NUM_BYTES < cluster){
				sb.append(getStringFromBytes(dirStarts.get(i) + OFFSET, NUM_BYTES - OFFSET));
				break;
			} 
			else if (OFFSET < cluster && NUM_BYTES > cluster){
				sb.append(getStringFromBytes(dirStarts.get(i) + OFFSET, bytesPerCluster - OFFSET));
				OFFSET = 0;
				NUM_BYTES -= bytesPerCluster;
			} 
			else {
				OFFSET -= bytesPerCluster;
				NUM_BYTES -= bytesPerCluster;
			}
		}
		String read = sb.toString();
		return read;
	}

	//most important method. goes to the directory where we want it to go
	public String goToDir(int dir, StringTokenizer st, String fullPath, String command) {
		// boolean error = false;
		int dirTrain = currentDIR;
		boolean found = false;
		while(st.hasMoreTokens()){
			found = false;
			String name = st.nextToken();
			if(name.equals("..")){
				if (parentMap.get(dirTrain) == null) {
					found = false;
					return "Error: No Directory Found";
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
						if (command.equals("stat") || command.equals("open") || command.equals("ls") || command.equals("close") || command.equals("size") || command.equals("read")) {
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
		String currentName = getStringFromBytes(dirTrain, 11);
		currentName = nameNice(currentName).trim();
		if (found == false){
			// error = true;
			return "Error: " + fullPath + " is not a directory";
		} 
		else if (command.equals("ls")) {
			return ls(dirTrain);
		} 
		else if (command.equals("stat")) {
			return stat(dirTrain);
		}
		else if (command.equals("cd")) {
			currentDIR = dirTrain;
		} 
		else if (command.equals("size")) {
			return "Size of " + fullPath + " is " + size(dirTrain) + " bytes";
		} 
		else if (command.equals("read")){
			if(size(dirTrain) <= OFFSET + NUM_BYTES) 
				return "Error: attempt to read data outside of file bounds";
			else{
				return fileReader(dirTrain);
			}
		}
		else if ((command.equals("open") || command.equals("close") || command.equals("size")) && !currentName.contains(".")){
			return "false";
		}
		return "";
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

	@Override
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

	public int getBPB_BytsPerSec() {
		return BPB_BytsPerSec;
	}

	public void setBPB_BytsPerSec(int bPB_BytsPerSec) {
		BPB_BytsPerSec = bPB_BytsPerSec;
	}

	public int getBPB_SecPerClus() {
		return BPB_SecPerClus;
	}

	public void setBPB_SecPerClus(int bPB_SecPerClus) {
		BPB_SecPerClus = bPB_SecPerClus;
	}

	public int getBPB_RsvdSecCnt() {
		return BPB_RsvdSecCnt;
	}

	public void setBPB_RsvdSecCnt(int bPB_RsvdSecCnt) {
		BPB_RsvdSecCnt = bPB_RsvdSecCnt;
	}

	public int getBPB_NumFATs() {
		return BPB_NumFATs;
	}

	public void setBPB_NumFATs(int bPB_NumFATs) {
		BPB_NumFATs = bPB_NumFATs;
	}

	public int getBPB_FATSz32() {
		return BPB_FATSz32;
	}

	public void setBPB_FATSz32(int bPB_FATSz32) {
		BPB_FATSz32 = bPB_FATSz32;
	}

	public int getBPB_RootClus() {
		return BPB_RootClus;
	}

	public void setBPB_RootClus(int bPB_RootClus) {
		BPB_RootClus = bPB_RootClus;
	}

	public int getBPB_RootEntCnt() {
		return BPB_RootEntCnt;
	}

	public void setBPB_RootEntCnt(int bPB_RootEntCnt) {
		BPB_RootEntCnt = bPB_RootEntCnt;
	}

	public int getRootDirSectors() {
		return RootDirSectors;
	}

	public void setRootDirSectors(int rootDirSectors) {
		RootDirSectors = rootDirSectors;
	}

	public int getFirstDataSector() {
		return FirstDataSector;
	}

	public void setFirstDataSector(int firstDataSector) {
		FirstDataSector = firstDataSector;
	}

	public int getFATOffSet() {
		return FATOffSet;
	}

	public void setFATOffSet(int fATOffSet) {
		FATOffSet = fATOffSet;
	}

	public int getFatSecNum() {
		return FatSecNum;
	}

	public void setFatSecNum(int fatSecNum) {
		FatSecNum = fatSecNum;
	}

	public int getFATEntOffset() {
		return FATEntOffset;
	}

	public void setFATEntOffset(int fATEntOffset) {
		FATEntOffset = fATEntOffset;
	}

	public int getFirstSectorofCluster() {
		return FirstSectorofCluster;
	}

	public void setFirstSectorofCluster(int firstSectorofCluster) {
		FirstSectorofCluster = firstSectorofCluster;
	}

	public int getFatTableStart() {
		return FatTableStart;
	}

	public void setFatTableStart(int fatTableStart) {
		FatTableStart = fatTableStart;
	}

	public int getBytesPerCluster() {
		return bytesPerCluster;
	}

	public void setBytesPerCluster(int bytesPerCluster) {
		this.bytesPerCluster = bytesPerCluster;
	}

	public int getClustInFat() {
		return clustInFat;
	}

	public void setClustInFat(int clustInFat) {
		this.clustInFat = clustInFat;
	}

	public int getCurrentDIR() {
		return currentDIR;
	}

	public void setCurrentDIR(int currentDIR) {
		this.currentDIR = currentDIR;
	}

	public int getRoot() {
		return root;
	}

	public void setRoot(int root) {
		this.root = root;
	}

	public int getOFFSET() {
		return OFFSET;
	}

	public void setOFFSET(int oFFSET) {
		OFFSET = oFFSET;
	}

	public int getNUM_BYTES() {
		return NUM_BYTES;
	}

	public void setNUM_BYTES(int nUM_BYTES) {
		NUM_BYTES = nUM_BYTES;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public ArrayList<String> getLsList() {
		return lsList;
	}

	public void setLsList(ArrayList<String> lsList) {
		this.lsList = lsList;
	}

	public HashMap<Integer, Integer> getParentMap() {
		return parentMap;
	}

	public void setParentMap(HashMap<Integer, Integer> parentMap) {
		this.parentMap = parentMap;
	}

	public LinkedList<String> getCdList() {
		return cdList;
	}

	public void setCdList(LinkedList<String> cdList) {
		this.cdList = cdList;
	}

	public HashSet<String> getOpenList() {
		return openList;
	}

	public void setOpenList(HashSet<String> openList) {
		this.openList = openList;
	}
    
    
}
