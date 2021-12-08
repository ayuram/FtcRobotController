package org.firstinspires.ftc.teamcode.util.roadrunnerext

import com.acmerobotics.roadrunner.drive.DriveSignal
import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.kinematics.Kinematics
import com.acmerobotics.roadrunner.util.NanoClock
import com.acmerobotics.roadrunner.util.epsilonEquals
import org.firstinspires.ftc.teamcode.util.roadrunnerext.RamseteConstants.*
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Time-varying, non-linear feedback controller for nonholonomic drives. See equation 5.12 of
 * [Ramsete01.pdf](https://www.dis.uniroma1.it/~labrob/pub/papers/Ramsete01.pdf).
 *
 * @param b b parameter (non-negative)
 * @param zeta zeta parameter (on (0, 1))
 * @param admissibleError admissible/satisfactory pose error at the end of each move
 * @param timeout max time to wait for the error to be admissible
 * @param clock clock
 */
class ImprovedRamsete @JvmOverloads constructor(
    admissibleError: Pose2d = Pose2d(2.0, 2.0, Math.toRadians(5.0)),
    timeout: Double =  0.5,
    clock: NanoClock = NanoClock.system(),
) : ImprovedTrajectoryFollower(admissibleError, timeout, clock) {
    override var lastError: Pose2d = Pose2d()

    private fun sinc(x: Double) =
        if (x epsilonEquals 0.0) {
            1.0 - x * x / 6.0
        } else {
            sin(x) / x
        }

    override fun internalUpdate(currentPose: Pose2d, currentRobotVel: Pose2d?): DriveSignal {
        val currentPose = currentPose.toMeters()
        val t = elapsedTime()
        val targetPose = trajectory[t].toMeters()
        val targetVel = trajectory.velocity(t).toMeters()
        val targetAccel = trajectory.acceleration(t).toMeters()

        val targetRobotVel = Kinematics.fieldToRobotVelocity(targetPose.toInches(), targetVel.toInches()).toMeters()
        val targetRobotAccel = Kinematics.fieldToRobotAcceleration(targetPose.toInches(), targetVel.toInches(), targetAccel.toInches()).toMeters()

        val targetV = targetRobotVel.x + (currentRobotVel?.let { kLinear * (targetRobotVel.toInches().x - hypot(it.x, it.y)) } ?: 0.0)
        val targetOmega = targetRobotVel.heading + (currentRobotVel?.let { kHeading * (targetRobotVel.toInches().heading - it.heading) } ?: 0.0)

        val error = Kinematics.calculateFieldPoseError(targetPose.toInches(), currentPose.toInches()).toMeters()

        val k1 = 2 * zeta * sqrt(targetOmega * targetOmega + b * targetV * targetV)
        val k3 = k1
        val k2 = b

        val v = targetV * cos(error.heading) +
                k1 * (cos(currentPose.heading) * error.x + sin(currentPose.heading) * error.y)
        val omega = targetOmega + k2 * targetV * sinc(error.heading) *
                (cos(currentPose.heading) * error.y - sin(currentPose.heading) * error.x) +
                k3 * error.heading

        lastError = Kinematics.calculateRobotPoseError(targetPose.toInches(), currentPose.toInches())

        return DriveSignal(Pose2d(v, 0.0, omega).toInches(), targetRobotAccel.toInches())
    }
}
