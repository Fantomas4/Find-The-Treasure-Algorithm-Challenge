import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Mines {

    List<int[]> minesPos = new ArrayList<>();
    int[] startPos = new int[2];
    int[] treasurePos = new int[2];

    Set<int[]> hull = new HashSet<>();

//    DIAG!!!
    int counter = 0;

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
        this.treasurePos = minesPos.remove(0);

//        // DIAG ONLY!!!
//        System.out.println("================ DIAG ================");
//        System.out.printf("*** Starting position is: %d %d ***\n", this.startPos[0], this.startPos[1]);
//        System.out.printf("*** Treasure position is: %d %d ***\n", this.treasurePos[0], this.treasurePos[1]);
//        for (int[] point : this.minesPos) {
//            System.out.printf("%d %d\n", point[0], point[1]);
//        }
    }

    private double pointsDistance(int[] point1, int[] point2) {
        return Math.sqrt(Math.pow(point2[0] - point1[0], 2) + Math.pow(point2[1] - point1[1], 2));
    }

    public int[] maxPointUsingAngle(int[] tp1, int[] tp2, int[] p1, int[] pn) {
        double p1pnDistance = pointsDistance(p1, pn);

        // Calculate the angle for test point 1 (tp1)
        double tp1pnDistance = pointsDistance(tp1, pn);
        double tp1p1Distance = pointsDistance(tp1, p1);
        double angle1 = Math.acos((Math.pow(tp1pnDistance, 2) + Math.pow(tp1p1Distance, 2) - Math.pow(p1pnDistance, 2))
                / (2 * tp1pnDistance * tp1p1Distance));

        // Calculate the angle for test point 2 (tp2)
        double tp2pnDistance = pointsDistance(tp2, pn);
        double tp2p1Distance = pointsDistance(tp2, p1);
        double angle2 = Math.acos((Math.pow(tp2pnDistance, 2) + Math.pow(tp2p1Distance, 2) - Math.pow(p1pnDistance, 2))
                / (2 * tp2pnDistance * tp2p1Distance));

        if (angle1 >= angle2) {
            return tp1;
        } else {
            return tp2;
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

//        DIAG!!!
        System.out.println(counter);
        counter += 1;

        int maxIndex = -1;
        int maxDistance = 0;

        //Find the point with the maximum distance from line p1pn
        for (int i = 0; i < minesPos.size(); i++) {
            int tempDistance = pointToLineDist(minesPos.get(i), p1, pn);
            if (determinePointSide(minesPos.get(i), p1, pn) == side) {
                if (tempDistance > maxDistance) {
//                If the current mine point has a greater distance from p1pn than maxDistance
                    maxIndex = i;
                    maxDistance = tempDistance;
                } else if (tempDistance == maxDistance) {
                    // **** WIP: Calculate angle - slide 196
                    if (maxPointUsingAngle(minesPos.get(maxIndex), minesPos.get(i), p1, pn) ==
                            minesPos.get(i)) {
                        maxIndex = i;
                        maxDistance = tempDistance;
                    }
                }
            }
        }

        if (maxIndex == -1) {
            System.out.println("MPIKA");
            //If no points are found, add p1 and pn to the
            // appropriate Sub-Hull arraylist
//            if (side == 1) {
//                topSubHull.add(p1);
//                topSubHull.add(pn);
//            } else if (side == -1) {
//                bottomSubHull.add(p1);
//                bottomSubHull.add(pn);
//            }
            hull.add(p1);
            hull.add(pn);
            return;
        }
        System.out.println("MPIKA2");
        // Na afairw apo to minesPos osa stoixeia elegxw oste na meiwsw ton arithmo epanalipseon
        // stiw epomenes anazitiseis quickhull?

        quickhull(minesPos.get(maxIndex), p1, -determinePointSide(pn, minesPos.get(maxIndex), p1));
        quickhull(minesPos.get(maxIndex), pn, -determinePointSide(p1, minesPos.get(maxIndex), pn));
    }

    public void findShortestPath() {
        quickhull(startPos, treasurePos, 1);
//        double topPathDistance = 0;
//        for (int i = 0; i < topSubHull.size() - 1; i++) {
//            topPathDistance += pointsDistance(topSubHull.get(i), topSubHull.get(i + 1));
//        }
//
        quickhull(startPos, treasurePos, -1);
//        double bottomPathDistance = 0;
//        for (int i = 0; i < bottomSubHull.size() - 1; i++) {
//            bottomPathDistance += pointsDistance(bottomSubHull.get(i), bottomSubHull.get(i + 1));
//        }
//
//        // DIAG ONLY!!!
//        System.out.println(topPathDistance);
//        for (int[] point : topSubHull) {
//            System.out.printf("%d %d\n", point[0], point[1]);
//        }
//
//        System.out.println("\n\n");
//
//        System.out.println(bottomPathDistance);
//        for (int[] point : bottomSubHull) {
//            System.out.printf("%d %d\n", point[0], point[1]);
//        }
        for (int[] point : hull) {
            System.out.printf("%d %d\n", point[0], point[1]);
        }
    }

    public static void main(String[] args) {
//        System.out.println(args[0]);
        Mines mines = new Mines(args[0]);

//        int[] A = new int[]{0, 0};
//        int[] B = new int[]{0, 1};
//        int[] C = new int[]{1, 0};
//
//        mines.maxPointUsingAngle(A, new int[]{5, 5}, B, C);

        mines.findShortestPath();




    }
}
