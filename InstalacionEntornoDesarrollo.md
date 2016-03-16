En esta página se indica como instalar el entorno de desarrollo para contribuir en el desarrollo del Open EHR-Gen Framework.




# Introducción #

El entorno de desarrollo consta del siguiente software:

  * [JDK 1.6 o superior](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
  * [Grais 1.3.7](http://www.grails.org/Download)
  * [IDE Spring Source Tool Swite (STS)](http://www.springsource.com/developer/sts) (basado en _Eclipse_)
  * Un cliente SVN (recomendamos el plugin Subversive para STS)
  * Un Manejador de Base de Datos (recomendamos [MySQL Community Server](http://www.mysql.com/downloads/mysql/)).


# Detalles #

Luego de descargar e instalar la última versión del _Java Development Kit (JDK)_, de descargar _Grails_ y descomprimirlo en algún directorio adecuado, creamos dos variables de entorno:

_JAVA\_HOME_: Con la ruta al directorio en donde fue instalado _Java_.
y
_GRAILS\_HOME_: Con la ruta al directorio en donde fue descomprimido _Grails_

Posteriormente agregamos a la variable de entorno _path_, las siguientes rutas:
_%GRAILS\_HOME%\bin_ y _%JAVA\_HOME%\bin_

En cuanto a la instalación del IDE _Spring Source Tool Swite_ y de los plugins necesarios para la correcta integración con el lenguaje _Groovy_ y el _Framework Grails_, se recomienda seguir el [instructivo](http://grails.org/STS+Integration) que se encuentra en la página de _Grails_, que realmente es muy sencillo. Simplemente hay que cerciorarse que el IDE quede instalado en un directorio en el cual el usuario que utilice el IDE tenga permisos de escritura (pues sino dara problemas a la hora de instalar los plugins).

No es necesario utilizar este IDE para el desarrollo aunque si lo recomendamos. De todas manera podría utilizar directamente _Eclipse_ (con los plugins para soporte _Groovy_) o _NetBeans_ (que tiene integración completa con _Grails_, aunque con problemas de performance importantes).

Debemos instalar un cliente _SVN_. Si opto por trabajar con _STS_, recomendamos instalar el cliente [Subversive](http://www.eclipse.org/subversive/) de la misma forma que se instalaron los plugins para _Groovy_ y _Grails_.

Por ultimo, luego de descargados los fuentes con el cliente _SVN_ desde la dirección http://open-ehr-gen-framework.googlecode.com/svn/trunk/, debemos configurar la base de datos (ver la [wiki de Instalación del Framework](http://code.google.com/p/open-ehr-gen-framework/wiki/Instalacion)). Si desea utilizar un Manejador de Base de Datos diferente al recomendado (_MySQL_), deberá de proporcionar el adaptador correspondiente y almacenarlo en el directorio _lib_ del proyecto _Grails_.