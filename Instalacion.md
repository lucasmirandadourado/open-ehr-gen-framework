

# Introduction #

En esta página se intentara especificar todos los pasos necesarios para instalar y correr el framework.


# Requisitos #

Antes de instalar el framework debemos tener:
  * Java SDK 6 (JDK6):
    * [instalación página de Oracle (inglés)](http://www.oracle.com/technetwork/java/javase/index-137561.html)
    * [instalación paso a paso (inglés)](http://www.rose-hulman.edu/class/csse/resources/JavaDevKit/installation.htm)
  * Grails Framework 1.3.7: [descarga](http://www.grails.org/download/archive/Grails) [instalacion](http://www.grails.org/Installation)
    * La versión de Grails DEBE ser la 1.3.7.
      * Si desea probar actualizar la versión de Grails, considere: http://www.technipelago.se/content/technipelago/blog/grails-upgrade-1.3.7-to-2.0.1
  * Cliente SVN (solo para utilizar la versión de desarrollo)
    * Tortoise: http://tortoisesvn.tigris.org/
    * SVN desde Eclipse (plugin subclipse): http://subclipse.tigris.org
  * Para no instalar un cliente SVN, puedes bajar la última versión liberada en la página de descargas:
    * [descargas](http://code.google.com/p/open-ehr-gen-framework/downloads/list)
  * MySQL (no es obligatorio tenerlo): [descarga](http://www.mysql.com/downloads/mysql/)
    * Los datos de conexión a la base se configuran en /open-ehr-gen/grails-app/config/Datasource.groovy


## Auxiliares ##
  * Sobre variables de entorno en Windows: http://vlaurie.com/computers2/Articles/environment.htm
  * Sobre variables de entorno en Linux: [UBUNTU](https://help.ubuntu.com/community/EnvironmentVariables) [Slackware](http://www.slackbook.org/html/shell-bash.html#SHELL-BASH-ENVIRONMENT) [CentOS](http://www.centos.org/docs/4/4.5/System_Administration_Guide/Default_Settings-Environment_Variables.html)
  * Sobre variables de entorno en MacOS: http://dhptech.com/article/2007/02/09/setting-environment-variables-for-mac-osx-programs
  * Más sobre cómo setear las variables de entorno: http://softimage.wiki.softimage.com/xsidocs/EnvVars_SettingandUsingEnvironmentVariables.htm


# Instalacion #
  * Descargar el código del framework usando el cliente SVN apuntando a: http://open-ehr-gen-framework.googlecode.com/svn/trunk/
  * Desde la línea de comandos, ir al directorio donde se descargó el framework, y correrlo con Grails: "grails run-app"


# Configuración de la base de datos #

  * La conexión a la base de datos se configura en el archivo: /open-ehr-gen/grails-app/config/Datasource.groovy
  * Grails tiene 3 modos de funcionamiento: development, testing y production.
  * Grails define en Datasource.groovy una conexión por cada modo de funcionamiento.
  * Si no se aclara lo contrario, siempre estaremos usando el modo "development" o "dev".

La configuración de la base tiene este aspecto:

```
environments {
  development {
    dataSource {

      /* DB en MEMORIA
      dbCreate = "create-drop" // one of 'create', 'create-drop','update'
      url = "jdbc:hsqldb:mem:devDB"
      */

      // DB MySQL		
      pooling = true
      driverClassName = "com.mysql.jdbc.Driver"
      url = "jdbc:mysql://localhost:3306/oehr_dev?useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8"
      dbCreate = "create-drop"
      username = "root"
      password = "" 
    }
  }
  ...
```

Si se descomentan las líneas correspondientes a "DB en Memoria" y se comentan las correspondientes a "DB MySQL", no será necesario tener MySQL instalado, y Grails usará la base de datos en memoria que ya tiene incorporada.

_**Nota**: por defecto en modo "dev", cada vez que se baje y levante la aplicación, la base de datos será destruida y creada de nuevo._

_**Nota**: si se usa MySQL, se debe crear la base de datos antes de correr la aplicación con "grails run-app". Notar que en el archivo de configuración que viene en el paquete descargado, el nombre de la base es_oehr\_dev_._


## Instalación en Apache Tomcat ##

TODO
http://tomcat.apache.org/


## Instalación en JBoss AS ##

TODO
http://community.jboss.org/en/jbossas


## Instalación en Jetty ##

TODO
http://jetty.codehaus.org/jetty/