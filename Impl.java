import java.io.*;
import java.nio.file.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;

public class Impl extends UnicastRemoteObject implements Info, LS, Stat, CD, Open, Close, Read, Size, Init {

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

    @Override
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
    public void read(String command, int start, int end) throws RemoteException {
        
    }

    @Override
    public void close(String command) throws RemoteException {
        
    }

    @Override
    public void open(String command) throws RemoteException {
        
    }

    @Override
    public void cd(String command) throws RemoteException {
        
    }

    @Override
    public void stat(String command) throws RemoteException {
        
    }

    @Override
    public void ls(String command) throws RemoteException {
        
    }

    @Override
    public String info() throws RemoteException {
		System.out.println("THIS IS FROM THE SERVER");
        return "BPB_BytsPerSec: 0x" + Integer.toHexString(BPB_BytsPerSec) + ", " + BPB_BytsPerSec + 
        "\nBPB_SecPerClus: 0x" + Integer.toHexString(BPB_SecPerClus) + ", " + BPB_SecPerClus + 
        "\nBPB_RsvdSecCnt: 0x" + Integer.toHexString(BPB_RsvdSecCnt) + ", " + BPB_RsvdSecCnt +
        "\nBPB_NumFATs: 0x" + Integer.toHexString(BPB_NumFATs) + ", " + BPB_NumFATs +
        "\nBPB_FATSz32: 0x" + Integer.toHexString(BPB_FATSz32) + ", " + BPB_FATSz32;
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
