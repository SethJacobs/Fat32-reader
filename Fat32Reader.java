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

	public static void main(String[] args) throws IOException {
		Fat32Reader fr = new Fat32Reader();
		fr.initiate(args[0]);
		// fr.info();
		fr.ls("DIR\\A\\SPEC\\..\\..\\..");
		// while (true){
		// 	Scanner sc = new Scanner(System.in);
		// }
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

	public void ls(int dir){
		ArrayList<String> files = new ArrayList<>();
		if (currentDIR == root){
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
		for (String file : files) {
			System.out.print(file + " ");
		}
		currentDIR = root;
	}

	public void goToDir(int dir, StringTokenizer st, String fullPath) {
		boolean error = false;
		boolean found = false;
		Stack<Integer> path = new Stack<>();
		path.push(root);
		while(st.hasMoreTokens()){
			found = false;
			String name = st.nextToken();
			if(name.equals("..")){
				found = true;
				if (path.peek() == root) {
					System.out.println("Error: No Directory Found");
					error = true;
				} else{
					path.pop();
					currentDIR = path.peek();
				}
			} else if (currentDIR == root){
				for (int i = root;  i < root + bytesPerCluster; i += 64)  {
					int attr = getBytes(i+11, 1);
					String dirName = getStringFromBytes(i, 11);
					dirName = nameNice(dirName).trim();
					if (attr == 16 && !dirName.contains("~1") && i != root){
						if (dirName.equals(name)) {
							found = true;
							path.push(i);
							currentDIR = i;
							break;
						}
					}
				}
			} else {
				ArrayList<Integer> dirStarts = new ArrayList<Integer>();
				String low = Integer.toHexString(getBytes(currentDIR+ 26, 2));
				String hi = Integer.toHexString(getBytes(currentDIR + 20, 2));
				int firstclust = Integer.parseInt(hi + low, 16);
				// clustInFat = getBytes(i+26, 2);
				getDir(dirStarts, firstclust);
				for (Integer integer : dirStarts) {
					for (int j = integer+32; j < integer + bytesPerCluster; j+= 64) {
						String currentName = getStringFromBytes(j, 11);
						currentName = nameNice(currentName).trim();
						// System.out.println(currentName + counter++);
						if (currentName.equals(name)) {
							found = true;
							path.push(j);
							currentDIR = j;
							continue;
						}
					}
				}
			}
		}
		if (found == false){
			error = true;
			System.out.println("Error: " + fullPath + " is not a directory");
		}
		if(error == false){ 
			ls(currentDIR);
		}
	}

	public void splitPath(String path) {
		StringTokenizer st = new StringTokenizer(path, "\\");
		goToDir(root, st, path);
		// changeDirectory(st.nextToken());
	}

	public void ls(String dirName){
		switch (dirName) {
			case ".":
				ls(currentDIR);
				break;
			case "..":
				if(currentDIR != root){
					ls(currentDIR);
				} else System.out.println("Error: No Directory Found");
				
				break;
			
			default:
				splitPath(dirName);
				break;
		}
	}

	public void stat() {
		
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
	
}