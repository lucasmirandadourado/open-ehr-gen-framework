En Windows: Cambiar el grails/bin/startGrails.bat, la linea 127 por:

```
if "%JAVA_OPTS%" == "" set JAVA_OPTS=-Xms200m -Xmx700m -XX:MaxPermSize=128m
```

En Linux: Cambiar el grails/bin/startGrails, la linea 231 por:
```
JAVA_OPTS="-server -Xms200m -Xmx700m -XX:MaxPermSize=128m"
```



# Sobre el PermGen size: #

http://www.brokenbuild.com/blog/2008/01/29/the-what-and-why-of-fixing-javalangoutofmemoryerror-permgen-space/

http://www.programacionenjava.com/blog/2008/06/24/problemas-frecuentes/aumentar-el-tamano-de-memoria-de-la-maquina-virtual-en-java/



# Sobre cuanta memoria está bien asignar como máximos: #

http://java.sun.com/docs/hotspot/gc1.4.2/faq.html



# Correr en puerto #

grails -Dserver.port=8090 run-app