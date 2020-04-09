import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Mines {

    List<int[]> minesPos = new ArrayList<>();
    int[] startPos = new int[2];
    int[] treasurePos = new int[2];

    Mines(String inputFileDir) {
        loadFile(inputFileDir);
    }

    private void loadFile(String dir) {
        File file = new File(dir);
        Scanner scanner;
        String line;
        String[] coordinates;

        try {
            scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();

                if (!line.equals("")) {
                    System.out.println(line);
                    coordinates = line.split(" ");

                    int[] point = new int[2];
                    point[0] = Integer.parseInt(coordinates[0]);
                    point[1] = Integer.parseInt(coordinates[1]);

                    minesPos.add(point);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("*** Error: File not Found. ***");
            return;
        }

        this.startPos = minesPos.remove(0);
        this.treasurePos = minesPos.remove(minesPos.size() - 1);

        System.out.println("================ DIAG ================");
        System.out.printf("*** Starting position is: %d %d ***\n", this.startPos[0], this.startPos[1]);
        System.out.printf("*** Treasure position is: %d %d ***\n", this.treasurePos[0], this.treasurePos[1]);
        for (int[] point : this.minesPos) {
            System.out.printf("%d %d\n", point[0], point[1]);
        }
    }

    //    ************ ATTENTION : slide 197, book pdf
    private int determinePointSide(int[] targetPoint, int[] linePoint1, int[] linePoint2) {
        int value = (targetPoint[1] - linePoint1[1]) * (linePoint2[0] - linePoint1[0]) -
                (linePoint2[1] - linePoint1[1]) * (targetPoint[0] - linePoint1[0]);

        if (value > 0) {
            return 1;
        } else if (value < 0) {
            return -1;
        } else {
            return 0;
        }
    }

    //    ************ ATTENTION : slide 197, book pdf
    private int pointToLineDist(int[] targetPoint, int[] linePoint1, int[] linePoint2) {
        return Math.abs((targetPoint[1] - linePoint1[1]) * (linePoint2[0] - linePoint1[0]) -
                (linePoint2[1] - linePoint1[1]) * (targetPoint[0] - linePoint1[0]));
    }

    private void quickhull(int[] p1, int[] pn, int side) {

    }

    public static void main(String[] args) {
//        System.out.println(args[0]);
        Mines mines = new Mines(args[0]);


    }
}
