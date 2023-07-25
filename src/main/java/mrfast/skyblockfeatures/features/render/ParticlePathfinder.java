package mrfast.skyblockfeatures.features.render;

import net.minecraft.util.Vec3;
import java.util.ArrayList;
import java.util.List;

public class ParticlePathfinder {
    private static double distance(Vec3 v1, Vec3 v2) {
        double dx = v2.xCoord - v1.xCoord;
        double dy = v2.yCoord - v1.yCoord;
        double dz = v2.zCoord - v1.zCoord;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static Vec3 findClosestPoint(Vec3 currentPoint, List<Vec3> points) {
        Vec3 closestPoint = null;
        double minDistance = Double.MAX_VALUE;

        for (Vec3 point : points) {
            double distance = distance(currentPoint, point);
            if (distance < minDistance) {
                minDistance = distance;
                closestPoint = point;
            }
        }

        return closestPoint;
    }

    public static List<Vec3> findPath(List<Vec3> points) {
        List<Vec3> path = new ArrayList<>();
        List<Vec3> remainingPoints = new ArrayList<>(points);

        Vec3 currentPoint = points.get(0); // Start at the first point
        path.add(currentPoint);

        remainingPoints.remove(currentPoint);

        while (!remainingPoints.isEmpty()) {
            Vec3 closestPoint = findClosestPoint(currentPoint, remainingPoints);

            if (closestPoint != null) {
                path.add(closestPoint);
                currentPoint = closestPoint;
                remainingPoints.remove(closestPoint);
            } else {
                // If no closest point found, randomly select a remaining point
                int randomIndex = (int) (Math.random() * remainingPoints.size());
                Vec3 randomPoint = remainingPoints.get(randomIndex);
                path.add(randomPoint);
                currentPoint = randomPoint;
                remainingPoints.remove(randomPoint);
            }
        }

        return path;
    }


}
