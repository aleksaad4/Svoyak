package ru.ad4.svoyak

import com.jcabi.log.Logger
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.env.Environment
import org.springframework.core.env.SimpleCommandLinePropertySource
import ru.ad4.svoyak.config.Constants
import java.net.InetAddress
import java.net.UnknownHostException
import javax.annotation.PostConstruct
import javax.inject.Inject

@ComponentScan
@EnableAutoConfiguration
class App @Inject constructor(val env: Environment) {

    /**
     * Initializes App
     *
     *
     * Spring profiles can be configured with a program arguments --spring.profiles.active=your-active-profile
     *
     *
     */
    @PostConstruct
    fun initApplication() {
        if (env.activeProfiles.size == 0) {
            Logger.warn(this, "No Spring profile configured, running with default configuration")
        } else {
            Logger.info(this, "Running with Spring profile(s) : [${env.activeProfiles}]")
            val activeProfiles = env.activeProfiles.toSet()
            if (activeProfiles.contains(Constants.SPRING_PROFILE_DEVELOPMENT)
                    && activeProfiles.contains(Constants.SPRING_PROFILE_PRODUCTION)) {
                Logger.error(this, "You have misconfigured your application! "
                        + "It should not run with both the 'dev' and 'prod' profiles at the same time.")
            }
        }
    }

    companion object {
        /**
         * Main method, used to run the application.

         * @param args the command line arguments
         * *
         * @throws UnknownHostException if the local host name could not be resolved into an address
         */
        @Throws(UnknownHostException::class)
        @JvmStatic fun main(args: Array<String>) {
            val app = SpringApplication(App::class.java)
            val source = SimpleCommandLinePropertySource(*args)
            addDefaultProfile(app, source)
            val env = app.run(*args).environment
            Logger.info(this, "\n----------------------------------------------------------\n\t"
                    + "Application '${env.getProperty("spring.application.name")}' is running! Access URLs:\n\t"
                    + "Local: \t\thttp://127.0.0.1:${env.getProperty("server.port")}\n\t"
                    + "External: \thttp://${InetAddress.getLocalHost().hostAddress}:${env.getProperty("server.port")}"
                    + "\n----------------------------------------------------------")
        }

        /**
         * If no profile has been configured, set by default the "dev" profile.
         */
        private fun addDefaultProfile(app: SpringApplication, source: SimpleCommandLinePropertySource) {
            if (!source.containsProperty("spring.profiles.active")
                    && !System.getenv().containsKey("SPRING_PROFILES_ACTIVE")) {
                app.setAdditionalProfiles(Constants.SPRING_PROFILE_DEVELOPMENT)
            }
        }
    }
}
