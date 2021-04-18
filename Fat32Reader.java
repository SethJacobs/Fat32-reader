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
		fr.info();
		fr.ls();
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
		root = getDir(list, BPB_RootClus);
		
		for (int i = root;  i < root + bytesPerCluster; i += 64)  {
			int attr = getBytes(i+11, 1);
			String dir = getStringFromBytes(i, 11);
			String name = nameNice(dir);
			if (attr == 8){
				root = i;
			} else if (attr == 16 && !dir.contains("~1") && i != root){
				System.out.println("printing this dir " + name);
				// String low = Integer.toHexString(getBytes(i + 26, 2));
				// String hi = Integer.toHexString(getBytes(i + 20, 2));
				ArrayList<Integer> list2 = new ArrayList<>();
				// getDir(list2, Integer.parseInt(hi+low,16));
				clustInFat = getBytes(i+26, 2);
				currentDIR = clustInFat;
				getDir(list2, clustInFat);
				// name(list2);
			}
		}
		currentDIR = root;
	}

	// public void name(ArrayList<Integer> list) {
	// 	for (int j : list) {
	// 		for (int i = j-32;  i < j + bytesPerCluster; i += 64)  {
	// 			// int attr = getBytes(i+11, 1);
	// 			String dir = getStringFromBytes(i, 11);
	// 			String name = nameNice(dir);
	// 			System.out.println(name);
	// 			// if (attr == 8) {
	// 			// 	root = new Directory(null, i, name);
	// 			// } else if (attr == 16 && !dir.contains("~1") && i != j){
	// 			// 	root.addChild(new Directory(root, i, name));
	// 			// 	System.out.println("printing this dir " + name);
	// 			// 	ArrayList<Integer> test = new ArrayList<>();
	// 			// 	getDir(test, i);
	// 			// 	for(Integer inte : test)
	// 			// 	{
	// 			// 		System.out.println(inte);
	// 			// 	}
	// 			// }
	// 		}
	// 	}
	// }

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
	int counter = 0;
	public void ls(){
		boolean end = false;
		for (int i = currentDIR;  i < currentDIR + bytesPerCluster; i += 64)  {
			if(getBytes(i,1) == 0){
				end = true;
				break; 
			}
			int attr = getBytes(i+11, 1);
			String dir = getStringFromBytes(i, 11);
			String name = nameNice(dir).trim();
			// System.out.println(name);
			// if(getBytes(1,1) == 0x58) System.out.println(name + " hiiiiii");
			if (attr == 8) {
				root = i;
			} else if (!dir.contains("~1") && i != root){
				System.out.println(name + " " + counter++);
				// // String low = Integer.toHexString(getBytes(i + 26, 2));
				// // String hi = Integer.toHexString(getBytes(i + 20, 2));
				// ArrayList<Integer> list2 = new ArrayList<>();
				// // getDir(list2, Integer.parseInt(hi+low,16));
				// clustInFat = getBytes(i+26, 2);
				// currentDIR = clustInFat;
				// getDir(list2, clustInFat);
				// name(list2);
			}
		}
		if (end == false){
			currentDIR = getBytes(FatTableStart + (clustInFat * 4), 4);
			if(currentDIR <= 268435447) {
				clustInFat = currentDIR;
				currentDIR = getDir(new ArrayList<Integer>(), currentDIR) + 32;
				ls();
			}
			
		}
	// 	for (String dir : lsList) {
	// 		if(dir.endsWith("   ")){
	// 			System.out.print(dir.replaceAll(" ", ""));
	// 			continue;
	// 		}
	// 		dir = dir.replaceAll(" ", "");
	// 		StringBuilder sb = new StringBuilder(dir);
	// 		sb.insert(dir.length()-3, ".");
	// 		System.out.print(sb.toString() + " ");
	// 	}
	// 	System.out.println();
		
	}

	public void ls(String dirName){
		switch (dirName) {
			case ".":
				ls();
				break;
			case "..":
				if(currentDIR != root){
					// currentDIR = currentDIR.parent;
					// ls();
					ls();
					
				} else System.out.println("Error!!!");
				
				break;
			
			default:
				splitPath(dirName);
				break;
		}
	}

	public void splitPath(String path) {
		StringTokenizer st = new StringTokenizer(path, "\\");
		changeDirectory(st.nextToken());
		
	}

	public void changeDirectory(String name) {
		//loop through current directory folders
		for (int i = 0; i < data.length; i++) {
			if ("filename".equals(name)){
				splitPath("fileName");
			}
		}
	}

	public int nextDir(int dir) {
		currentDIR = getBytes(dir+26, 2);
		return getDir(new ArrayList<Integer>(), clustInFat);
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
        int firstSectorofDirCluster = ((n - 2) * BPB_SecPerClus) + FirstDataSector;
        int startOfDir = firstSectorofDirCluster * BPB_BytsPerSec;
		bytesPerCluster = BPB_BytsPerSec * BPB_SecPerClus;
		list.add(startOfDir);
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