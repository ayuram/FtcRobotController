package org.firstinspires.ftc.teamcode.drive

import com.acmerobotics.roadrunner.geometry.Pose2d
import org.firstinspires.ftc.teamcode.modules.vision.Detector
import org.firstinspires.ftc.teamcode.util.field.Alliance
import org.firstinspires.ftc.teamcode.util.field.Details
import org.firstinspires.ftc.teamcode.util.field.Details.side
import org.firstinspires.ftc.teamcode.util.field.Details.alliance
import org.firstinspires.ftc.teamcode.util.field.Details.location
import org.firstinspires.ftc.teamcode.util.field.Side
import org.firstinspires.ftc.teamcode.util.roadrunnerext.polarAdd

object FrequentPositions {
    fun flipSide(pose2d: Pose2d): Pose2d {
        return Pose2d(pose2d.x, -pose2d.y, -pose2d.heading)
    }

    @JvmStatic
    fun startingPosition(): Pose2d {
        val regular = if (Details.side == Side.CYCLING) Pose2d(8.2, -58.0, Math.toRadians(-90.0)) else Pose2d(
            -36.0,
            -58.0,
            Math.toRadians(-90.0)
        )
        return if (Details.alliance == Alliance.RED) regular else flipSide(regular)
    }

    fun dumpPosition(): Pose2d {
        val regular =
            if (Details.side == Side.CYCLING) Pose2d(5.0, -30.0, Math.toRadians(-15.0)) else Pose2d(
                -28.0,
                -31.0,
                Math.toRadians(20.0)
            )
        return if (Details.alliance == Alliance.RED) regular else flipSide(regular)
    }

    fun cycleDumpPosition(): Pose2d {
        val regular =
            if (side == Side.CYCLING) Pose2d(6.0, -32.0, Math.toRadians(-30.0)) else Pose2d()
        return if (Details.alliance == Alliance.RED) regular else flipSide(regular)
    }

    fun duckLocation(): Pose2d {
        val arr = duckLocations()
        when (location) {
            Detector.Location.LEFT -> return arr[0]
            Detector.Location.RIGHT -> return arr[2]
            Detector.Location.MIDDLE -> return arr[1]
        }
        return arr[0]
    }

    fun duckLocation(location: Detector.Location?): Pose2d {
        val arr = duckLocations()
        when (location) {
            Detector.Location.LEFT -> return arr[0]
            Detector.Location.RIGHT -> return arr[2]
            Detector.Location.MIDDLE -> return arr[1]
        }
        return arr[0]
    }

    fun duckLocations(): Array<Pose2d> {
        return if (alliance == Alliance.RED) {
            if (side == Side.CAROUSEL) {
                arrayOf(
                    Pose2d(
                        -32.0,
                        -44.0,
                        Math.toRadians(8.0)
                    ),
                    Pose2d(
                        -32.0,
                        -44.0,
                        Math.toRadians(15.0)
                    ),
                    Pose2d(
                        -32.0,
                        -44.0,
                        Math.toRadians(30.0)
                    )
                )
            } else {
                arrayOf(
                    Pose2d(
                        1.0,
                        -44.0,
                        Math.toRadians(0.0)
                    ),
                    Pose2d(
                        6.6,
                        -44.0,
                        Math.toRadians(0.0)
                    ),
                    Pose2d(
                        9.0,
                        -44.0,
                        Math.toRadians(-25.0)
                    )
                )
            }
        } else {
            if (side == Side.CAROUSEL) {
                arrayOf(
                    Pose2d(
                        -32.0,
                        44.0,
                        Math.toRadians(0.0)
                    ),
                    Pose2d(
                        -40.0,
                        44.0,
                        Math.toRadians(0.0)
                    ),
                    Pose2d(
                        -50.0,
                        44.0,
                        Math.toRadians(0.0)
                    )
                )
            } else {
                arrayOf(
                    Pose2d(9.9, 44.0, Math.toRadians(0.0)),
                    Pose2d(
                        9.1,
                        46.0,
                        Math.toRadians(25.0)
                    ).polarAdd(1.0),
                    Pose2d(
                        1.0,
                        44.0,
                        Math.toRadians(-25.0)
                    )
                )
            }
        }
    }
}