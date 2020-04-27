import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Mines {

    List<int[]> minesPos = new ArrayList<>();
    int[] startPos = new int[2];
    int[] treasurePos = new int[2];
    
    List<int[]> candidatePoints = new ArrayList<>();

    Set<int[]> hull = new HashSet<>();
    List<int[]> topSubHull = new ArrayList<>();
    List<int[]> bottomSubHull = new ArrayList<>();

//    DIAG!!!
    int counter = 0;

    Mines(String inputFileDir) {

        if (loadFile(inputFileDir)) {
            findShortestPath();
        }
    }

    private boolean loadFile(String dir) {
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
            this.startPos = minesPos.remove(0);
            this.treasurePos = minesPos.remove(0);
            return true;
        } catch (FileNotFoundException e) {
//            e.printStackTrace();
            System.out.println("*** Error: File not Found. ***");
            return false;
        }



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

        // Na afairw apo to minesPos osa stoixeia elegxw oste na meiwsw ton arithmo epanalipseon
        // stiw epomenes anazitiseis quickhull?

        quickhull(minesPos.get(maxIndex), p1, -determinePointSide(pn, minesPos.get(maxIndex), p1));
        quickhull(minesPos.get(maxIndex), pn, -determinePointSide(p1, minesPos.get(maxIndex), pn));
    }

    private void findConvexHull() {
        quickhull(startPos, treasurePos, 1);
        quickhull(startPos, treasurePos, -1);

//        //        DIAG !!!
//        System.out.println("Convex hull is: ");
//        for (int[] point : hull) {
//            System.out.printf("%d %d\n", point[0], point[1]);
//        }
    }

    private void findSubHulls() {
        for (int[] point : hull) {
            int side = determinePointSide(point, startPos, treasurePos);
            if (side == 1) {
                topSubHull.add(point);
            } else if (side == -1) {
                bottomSubHull.add(point);
            }
        }
    }

    private List<int[]> merge(List<int[]> subList1, List<int[]> subList2) {
        int i = 0;
        int j = 0;

        List<int[]> mergedList = new ArrayList<>();

        int size1 = subList1.size();
        int size2 = subList2.size();
        while (i < size1 && j < size2) {
            // Compare points based on their x value
            if (subList1.get(i)[0] <= subList2.get(j)[0]) {
                mergedList.add(subList1.get(i));
                i += 1;
            } else {
                mergedList.add(subList2.get(j));
                j += 1;
            }
        }

        if (i == size1) {
            while (j < size2) {
                mergedList.add(subList2.get(j));
                j += 1;
            }
        } else {
            while (i < size1) {
                mergedList.add(subList1.get(i));
                i += 1;
            }
        }

        return mergedList;
    }

    private List<int[]> mergeSort(List<int[]> initialList) {
        double size = initialList.size();

        if (size > 1) {
            List<int[]> subList1 = new ArrayList<>();
            for (int i = 0; i < Math.floor(size / 2); i++) {
                subList1.add(initialList.get(i));
            }

            List<int[]> subList2 = new ArrayList<>();
            for (int i = (int) Math.floor(size / 2); i < size; i++) {
                subList2.add(initialList.get(i));
            }

            subList1 = mergeSort(subList1);
            subList2 = mergeSort(subList2);
            initialList = merge(subList1, subList2);
        }

        return initialList;
    }

    private double calculatePathDistance(List<int[]> pathPoints) {
        double totalDistance = 0;

        for (int i = 0; i < pathPoints.size() - 1; i++) {
            totalDistance += pointsDistance(pathPoints.get(i), pathPoints.get(i + 1));
        }

        return totalDistance;
    }

    private String pathPointsString(List<int[]> pathPoints) {
        StringBuilder stringBuilder = new StringBuilder();

        int size = pathPoints.size();
        for (int i = 0; i < size - 1; i++) {
            stringBuilder.append(String.format("(%d,%d)-->", pathPoints.get(i)[0], pathPoints.get(i)[1]));
        }
        stringBuilder.append(String.format("(%d,%d)", pathPoints.get(size - 1)[0], pathPoints.get(size - 1)[1]));

        return stringBuilder.toString();
    }

    public void findShortestPath() {
        findConvexHull();
        findSubHulls();


        List<int[]> topSortedPath = mergeSort(topSubHull);
        // Add start position to the start of the sorted top path list
        topSortedPath.add(0, startPos);
        // Add treasure position to the end of the sorted top path list
        topSortedPath.add(treasurePos);
//        //        DIAG !!!
//        System.out.println("===================");
//        for (int[] point : topSortedPath) {
//            System.out.printf("%d %d\n", point[0], point[1]);
//        }

        List<int[]> bottomSortedPath = mergeSort(bottomSubHull);
        // Add start position at the start of the sorted bottom path list
        bottomSortedPath.add(0, startPos);
        // Add treasure position at the end of the sorted bottom path list
        bottomSortedPath.add(treasurePos);
//        //        DIAG !!!
//        System.out.println("===================");
//        for (int[] point : bottomSortedPath) {
//            System.out.printf("%d %d\n", point[0], point[1]);
//        }

        double topPathDistance = calculatePathDistance(topSortedPath);
        double bottomPathDistance = calculatePathDistance(bottomSortedPath);

        if (topPathDistance <= bottomPathDistance) {
            System.out.printf("The shortest distance is %.5f\n", topPathDistance);
            System.out.println("The shortest path is:" + pathPointsString(topSortedPath));
        } else {
            System.out.printf("The shortest distance is %.5f\n", bottomPathDistance);
            System.out.println("The shortest path is:" + pathPointsString(bottomSortedPath));
        }


    }

    public static void main(String[] args) {
        Mines mines = new Mines(args[0]);

    }
}
