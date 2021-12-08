package org.firstinspires.ftc.teamcode.localizers.t265

import com.acmerobotics.roadrunner.kinematics.TankKinematics
import com.arcrobotics.ftclib.geometry.Rotation2d
import com.arcrobotics.ftclib.geometry.Transform2d
import com.arcrobotics.ftclib.geometry.Translation2d
import com.intel.realsense.librealsense.UsbUtilities
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.util.RobotLog
import com.spartronics4915.lib.T265Camera
import org.firstinspires.ftc.teamcode.drive.DriveConstants.*
import org.firstinspires.ftc.teamcode.util.*
import org.firstinspires.ftc.teamcode.util.roadrunnerext.*

/**
 * Static accessibility class which allows for easy
 * creation and access of the T265 camera, wrapping
 * the FTC265 library to allow more straightforward
 * usage.
 */
@Suppress("UNUSED")
object Easy265 {
    private const val TAG = "Easy265"
    private const val ODOMETRY_COVARIANCE = 0.95
    private const val INCH_TO_METER = 0.0254
    private val defaultTransform2d = Transform2d(
        Translation2d(-7.71 * INCH_TO_METER, 0.36 * INCH_TO_METER), Rotation2d(
            Math.toRadians(180.0)
        )
    )
    private lateinit var drive: ImprovedTankDrive


    /**
     * The global T265 camera, throws UninitializedPropertyAccessException
     * if the camera instance hasn't been created, call init() to do so
     */
    @JvmStatic lateinit var camera: T265Camera
        private set

    @JvmStatic val isStarted get() = ::camera.isInitialized && camera.isStarted

    @JvmStatic var velocity = com.acmerobotics.roadrunner.geometry.Pose2d()

    /**
     * The camera data returned from the last update.
     * Returns null if update() hasn't been called or the camera hasn't been started
     */
    @JvmStatic var lastCameraUpdate: T265Camera.CameraUpdate? = null

    /**
     * The pose reported from the last camera update, in inches.
     * Returns null if update() hasn't been called or the camera hasn't been started
     * The pose is only sent to the camera if it isn't null and the camera has been started.
     */
    @JvmStatic var poseHasBeenSet = false

    @JvmStatic var lastPose
        //set and get are in inches
        set(value) {
//            while (lastCameraUpdate?.confidence == T265Camera.PoseConfidence.Failed) {
//                update()
//                Details.telemetry?.update()
//            }
            if(value != null && isStarted && !poseHasBeenSet) {
                poseHasBeenSet = true
                camera.setPose(value.flipHeading().toFTCLibPose2d().toMeters())
            }
        }
        get() = lastCameraUpdate?.pose?.toRRPose2d()?.toInches()

    /**
     * The velocity reported from the last camera update.
     * Returns null if update() hasn't been called or the camera hasn't been started
     */
    @JvmStatic val lastVelocity get() = lastCameraUpdate?.velocity?.toRRPoseVelo()?.toInches()

    /**
     * Initializes and starts the camera. If it has already been started,
     * it updates the data. Recursively tries to open the camera the specified
     * amount of attempts.
     *
     * @param hardwareMap current HardwareMap (tp get the appContext)
     * @param cameraToRobot offset of the center of the robot from the center of the camera
     * @param attempts amount of times this method will be called recursively if the initialization fails
     */
    @JvmStatic @JvmOverloads fun init(
        opMode: OpMode,
        cameraToRobot: Transform2d = defaultTransform2d,
        attempts: Int = 6,
        drive: ImprovedTankDrive
    ) {
        this.drive = drive
        velocity = com.acmerobotics.roadrunner.geometry.Pose2d()
        try {
            if(!Easy265::camera.isInitialized) {
                UsbUtilities.grantUsbPermissionIfNeeded(opMode.hardwareMap.appContext)
                camera = T265Camera(cameraToRobot, ODOMETRY_COVARIANCE, opMode.hardwareMap.appContext)
                UsbUtilities.grantUsbPermissionIfNeeded(opMode.hardwareMap.appContext)
            }

            if(camera.isStarted) {
                update()
                return
            }
            camera.start()
            update()
        } catch(e: Exception) {
            if(attempts <= 0) {
                throw RuntimeException("Unable to start T265Camera after various attempts", e)
            } else {
                RobotLog.w(TAG, "Unable to start T265Camera, retrying...", e)
                init(opMode, cameraToRobot, attempts - 1, drive)
            }
        }
    }

    /**
     * Initializes and start the camera. If it has already been started,
     * it simply updates the data. Infinitely tries to recursively open the
     * camera. Not recommended.
     *
     * @param hardwareMap current HardwareMap (tp get the appContext)
     * @param cameraToRobot offset of the center of the robot from the center of the camera
     */
    @JvmStatic @JvmOverloads fun initWithoutStop(
        opMode: OpMode,
        cameraToRobot: Transform2d = defaultTransform2d,
        drive: ImprovedTankDrive
    ) = init(opMode, cameraToRobot, Int.MAX_VALUE, drive)

    /**
     * Pulls the last data from the camera and stores it the
     * lastCameraUpdate variable, therefore updating the data
     * returned by the lastPose or lastVelocity properties.
     *
     * If the camera hasn't been started (or instantiated) no update happens.
     */
    @JvmStatic fun update() {
        if(isStarted) {
            if (Thread.interrupted()) {
                camera.stop()
                return
            }
            lastCameraUpdate = camera.lastReceivedCameraUpdate
            velocity = TankKinematics.wheelToRobotVelocities(drive.getWheelVelocities() ?: listOf(0.0, 0.0), TRACK_WIDTH)
            if (USE_COVARIANCE) camera.sendOdometry(velocity.x * INCH_TO_METER, velocity.y * INCH_TO_METER)
        }
    }

    @JvmStatic fun stop() {
        camera.stop()
    }

}