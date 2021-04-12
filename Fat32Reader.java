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
	int FirstSectorofCluster, FatTableStart;
	byte[] data;

	public static void main(String[] args) throws IOException {
		Fat32Reader fr = new Fat32Reader();
		fr.initiate(args[0]);
		fr.info();
	}

	public void initiate(String paths) throws IOException {
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
		getDir(new ArrayList<>(), BPB_RootClus);
	}

	public void info() {
		System.out.println("BPB_BytsPerSec: 0x" + Integer.toHexString(BPB_BytsPerSec) + ", " + BPB_BytsPerSec);
		System.out.println("BPB_SecPerClus: 0x" + Integer.toHexString(BPB_SecPerClus) + ", " + BPB_SecPerClus);
		System.out.println("BPB_RsvdSecCnt: 0x" + Integer.toHexString(BPB_RsvdSecCnt) + ", " + BPB_RsvdSecCnt);
		System.out.println("BPB_NumFATs: 0x" + Integer.toHexString(BPB_NumFATs) + ", " + BPB_NumFATs);
		System.out.println("BPB_FATSz32: 0x" + Integer.toHexString(BPB_FATSz32) + ", " + BPB_FATSz32);
	}

	public void ls(){

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

	public void getDir(ArrayList<Integer> list, int start) {
		int n = start;
		FATOffSet = n * 4;
		FatSecNum = BPB_RsvdSecCnt + (FATOffSet / BPB_BytsPerSec);
		FatTableStart = FatSecNum * BPB_BytsPerSec;
		FATEntOffset = FATOffSet % BPB_BytsPerSec;
		int clusterOffset = FATEntOffset + FatTableStart;
        int nextClus = getBytes(clusterOffset, 4);
        int firstSectorofDirCluster = ((n - 2) * BPB_SecPerClus) + FirstDataSector;
        int startOfDir = firstSectorofDirCluster * BPB_BytsPerSec;
		list.add(startOfDir);
	}
	
}