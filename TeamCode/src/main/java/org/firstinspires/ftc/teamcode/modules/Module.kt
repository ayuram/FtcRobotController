package org.firstinspires.ftc.teamcode.modules
import com.acmerobotics.roadrunner.geometry.Pose2d
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.util.ElapsedTime
import com.qualcomm.robotcore.util.Range
import org.firstinspires.ftc.teamcode.roadrunnerext.polarAdd
import org.firstinspires.ftc.teamcode.util.field.Context
import org.firstinspires.ftc.teamcode.util.field.Context.robotPose
import kotlin.math.abs

/**
 * This abstract class represents any module or subcomponent of the robot
 * NOTE: Do NOT use "sleeps" or any blocking clauses in any functions
 * @param <T> The state type of the module
 * @author Ayush Raman
</T> */
abstract class Module<T : StateBuilder> @JvmOverloads constructor(
    @JvmField var hardwareMap: HardwareMap,
    private var _state: T,
    var poseOffset: Pose2d = Pose2d(),
    private var totalMotionDuration: Double = 1.0
) {
    /// State related fields
    private val elapsedTime = ElapsedTime()

    /**
     * @return The previous state of the module
     */
    var previousState: T = _state
        private set

    open var state: T
        /**
         * @return The state of the module
         */
        get() = _state
        /**
         * Set a new state for the module
         * @param value New state of the module
         */
        protected set(value) {
            if (state == value) return
            elapsedTime.reset()
            previousState = state
            _state = value
        }
    /**
     * The time spent in the current state in seconds
     */
    protected val secondsSpentInState
        get() = elapsedTime.seconds()

    /**
     * The time spent in the current state in milliseconds
     */
    protected val millisecondsSpentInState
        get() = elapsedTime.milliseconds()

    /**
     * The time spent in the current state in microseconds
     */
    protected val microsecondsSpentInState
        get() = millisecondsSpentInState * 1000

    /// Module utilities
    private var nestedModules = arrayOf<Module<*>>()
    val modulePoseEstimate: Pose2d
        get() = robotPose.polarAdd(poseOffset.x).polarAdd(poseOffset.y, Math.PI / 2)
    var isDebugMode = false
        set(value) {
            field = value
            for (module in nestedModules) {
                module.isDebugMode = value
            }
        }

    /**
     * This function initializes all necessary hardware modules
     */
    abstract fun internalInit()

    fun init() {
        internalInit()
        for (module in nestedModules) {
            module.init()
        }
    }

    /**
     * This function updates all necessary controls in a loop.
     * Prints the state of the module.
     * Updates all nested modules.
     */
    fun update() {
        for (module in nestedModules) {
            module.update()
        }
        internalUpdate()
        Context.packet.put(javaClass.simpleName + " State", if (isTransitioningState()) "$previousState --> $state" else state)
    }

    /**
     * @return Whether the module or its nested modules are currently hazardous
     */
    val isHazardous: Boolean
        get() {
            var isHazardous = isModuleInternalHazardous()
            for (module in nestedModules) {
                if (module.isHazardous) {
                    isHazardous = true
                }
            }
            return isHazardous
        }

    /**
     * @return Whether the module or its nested modules are currently doing work
     */
    val isDoingWork: Boolean
        get() {
            var isDoingWork = isDoingInternalWork()
            for (module in nestedModules) {
                if (module.isDoingWork) {
                    isDoingWork = true
                }
            }
            return isDoingWork
        }

    /**
     * Add any nested modules to be updated
     */
    fun setNestedModules(vararg modules: Module<*>) {
        nestedModules = modules as Array<Module<*>>
    }

    /**
     * @return nested modules
     */
    fun getNestedModules(): Array<Module<*>> {
        return nestedModules
    }

    /**
     * This function updates all necessary controls in a loop
     * Note: Do NOT update any nested modules in this method. This will be taken care of automatically
     */
    protected abstract fun internalUpdate()

    /**
     * @return Whether the module is currently doing work for which the robot must remain stationary
     */
    protected abstract fun isDoingInternalWork(): Boolean

    /**
     * @return Whether the module is currently in a hazardous state
     */
    protected abstract fun isModuleInternalHazardous(): Boolean

    /**
     * @return Whether the module is currently transitioning between states
     */
    abstract fun isTransitioningState(): Boolean
}