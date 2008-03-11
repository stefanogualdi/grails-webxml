/**
 * Copyright 2008 Roger Cass (roger.cass@byu.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class WebxmlGrailsPlugin {

    def DEFAULT_CONFIG_FILE = "DefaultWebXmlConfig"
    def APP_CONFIG_FILE     = "WebXmlConfig"

    def version = "1.0"
    def author = "Roger Cass"
    def authorEmail = "roger.cass@byu.net"
    def title = "Create useful additions to web.xml"
    def description = "Used the doWithWebDescriptor feature of plugins to create application-configured features otherwise unavailable."
    def documentation = "http://grails.org/WebXML+Plugin"
    def watchedResources = "**/grails-app/conf/${APP_CONFIG_FILE}.groovy"    
	
    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }
   
    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)		
    }

    def doWithWebDescriptor = { xml ->
    
        def config = getConfig()

        if(config) {
            if(config.filterChainProxyDelegator.add) {
                def contextParam = xml."context-param"
                    contextParam[contextParam.size() - 1] + {
                        'filter' {
                            'filter-name'(config.filterChainProxyDelegator.filterName)
                            'filter-class'(config.filterChainProxyDelegator.className)
                            'init-param' {
                                'param-name'('targetBeanName')
                                'param-value'(config.filterChainProxyDelegator.targetBeanName)
                            }
                        }
                    }
        
                def filter = xml."filter"
                    filter[filter.size() - 1] + {
                        'filter-mapping' {
                            'filter-name'(config.filterChainProxyDelegator.filterName)
                            'url-pattern'(config.filterChainProxyDelegator.urlPattern)
                        }
                    }
            }
        }
    }
	                                      
    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }
	
    def onChange = { event ->
        // TODO Implement code that is executed when this class plugin class is changed  
        // the event contains: event.application and event.applicationContext objects
    }
                                                                                  
    def onApplicationChange = { event ->
        // TODO Implement code that is executed when any class in a GrailsApplication changes
        // the event contain: event.source, event.application and event.applicationContext objects
    }
    
    def getConfig = {
        ClassLoader parent = getClass().getClassLoader()
        GroovyClassLoader loader = new GroovyClassLoader(parent)

        def config

        try {
            def defaultConfigFile = loader.loadClass(DEFAULT_CONFIG_FILE)
            //log.info("Loading default config file: "+defaultConfigFile)
            config = new ConfigSlurper().parse(defaultConfigFile)
            
            try {
                def appConfigFile = loader.loadClass(APP_CONFIG_FILE)
                //log.info("Found application config file: "+appConfigFile)
                def appConfig = new ConfigSlurper().parse(appConfigFile)
                if (appConfig) {
                    //log.info("Merging application config file: "+appConfigFile)
                    config = config.merge(appConfig)
                }
            } catch(ClassNotFoundException e) {
                //log.warn("Did not find application config file: "+APP_CONFIG_FILE)
            }
        } catch(ClassNotFoundException e) {
            //log.error("Did not find default config file: "+DEFAULT_CONFIG_FILE)
        }

        config?.webxml
    }
}
