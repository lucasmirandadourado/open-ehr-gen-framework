

# Ideas para optimización #

## Modelo de información ##

El modelo de información de openEHR es esencialmente jerárquico con una estructura de árbol de varios niveles. Esta estructura lo hace poco óptimo para realizar búsquedas, y complejiza el proceso de data binding, la persistencia (estructuras en BD relacional), y la generación de GUI con datos de estas estructuras.

La idea para optimizar este modelo es aplanarlo, conservando la estructura de árbol codificada (de forma que pueda ser construido un árbol real en cualquier momento a partir de datos planos. Para codificar la estructura de árbol se podrían usar las paths de los arquetipos openEHR que se deben guardar en el modelo de información, así teniendo el identificador del arquetipo y una path, se sabrá exactamente que lugar en el árbol ocupa cierto elemento.

### Sobre datos mínimos a almacenar ###

Los únicos datos que son necesarios almacenar para cumplir con los requerimientos de registro de los médicos, son los datos que ingresan en cada formulario, más algún dato de contexto como la fecha en que se ingresan los datos.

Por lo tanto, hacer el proceso de databinding en el momento que el usuario está ingresando datos es innecesario, porque no agrega valor a la información ingresada y demora entre 1 seg y 3 segs el proceso de binding, validación y almacenado de los datos en la base. Y luego mostrar los datos ingresados, demora entre 3 y 8 segs, dependiendo de la cantidad de datos y la cantidad de arquetipos referenciados desde el template.

La tarea que no puede evitarse es la de validar los datos ingresados, contra las restricciones presentes en los arquetipos y el template. Pero no sería necesario hacer el binding y el guardado en el momento del submit de información del médico.

Entonces los requerimientos básicos serían:
  * Generación del formulario de ingreso en base a metadatos.
  * Ingreso y validación de datos contra restricciones definidas en metadatos.
  * Generación de pantalla de edición con errores a corregir, en base a metadatos, datos ingresados y errores de validación detectados.
  * Generación de pantalla de visualización de datos ingresados correctamente, en base a metadatos y datos ingresados.
  * Capacidad de hacer búsquedas de datos por arquetipo y paths, restringidos por paciente, rango de fechas y dominio.
  * Capacidad de generar XMLs de COMPOSITIONS y EHR\_EXTRACTS (según RM de openEHR).


#### Generación de formularios de ingreso ####

  * Los metadatos deben ser completos: 1 solo template desde donde cuelgan todos los arquetipos necesarios, sin slots o internal refs (se resuelven todas las referencias antes de usar la estructura). Este es el proceso de Flattening o aplanamiento de la estructura, y debería resolverse y cachearse en tiempo de arranque, no en tiempo de ejecución.
  * Sobre ese template se deberían aplicar todas las restricciones de GUI necesarias para generar la vista (GUITemplate). Estas restricciones no se deberían aplicar en tiempo de ejecución, sino que se deberían calcular y aplicar en tiempo de arranque.
  * En base a lo anterior, deberían generarse y cachearse todos los formularios de ingreso de información, también en tiempo de arranque.
    * Debería haber un gestor del caché de vistas que permita regenerar las vistas y re-cachearlas sin necesidad de reiniciar la aplicación.
    * Cada vista debe referenciar al identificador del GUITemplate y la versión de ese GUITemplate. El GUITemplate tiene adentro contra qué template se aplica, con la versión a dicho template. Luego el template tiene referencias a todos los arquetipos que usa, con sus respectivas versiones.
  * Mostrar un formulario de ingreso de información luego de todo este proceso, debería hacerse en menos de un segundo.

  * Analizar la posibilidad de que la vista tenga una representación como un XForm, con un modelo XML asociado que sea el propio RM en XML.


#### Ingreso y validación de datos ####

  * En el mismo proceso que se generan las estructuras de metadatos completas para generar la vista de ingreso de datos, se deberían extraer las restricciones que aplican a cada nodo del RM.
  * La idea es que cada dato ingresado tendrá un arquetipo y una path asociada, y el objetivo es obtener las restricciones que aplican para cada dato ingresado.
    * Entonces la path del dato ingresado tendrá un conjunto de restricciones con las que debe cumplir.
    * Las restricciones pueden ser del propio nodo del arquetipo al que hace referencia cada dato (mediante su path), o a restricciones de los nodos ancestros (identificados por subpaths de la path de cada dato).
    * Las restricciones de los nodos ancestros, en general serán de ocurrencia y cardinalidad:
      * Un nodo debe ocurrir como mínimo X veces (0: opcional, 1: obligatorio, N>1: obligatorio múltiple)
      * Un nodo debe ocurrir como máximo Y veces (0: eliminado, 1: ocurrencia unitaria, N>1: múltiple acotada, **: múltiple no acotada)
      * Un atributo de tipo colección de un nodo debe tener cardinalidad mínima X (0: vacío, 1: requerido mínimo, N>1: requeridos múltiples)
      * Un atributo de tipo colección de un nodo debe tener cardinalidad máxima Y (0: sin hijos, 1: colección unitaria, N>1: colección múltiple acotada,**: colección múltiple no acotada).
    * Las restricciones de los nodos hoja, que serían los nodos padres inmediatos de las paths de los datos ingresados, pueden:
      * Tener restricciones sobre los valores de los datos ingresados (por ejemplo cumplir con rangos o que el valor debe estar en una lista de valores).
      * Tener restricciones propias del RM:
        * Opcionalidad y obligatoriedad de ciertos atributos (p.e. si un element, no tiene value, setear nullflavor).
        * Tipos estructurados: nodos del RM que tienen más de un atributo, y que se completan con varios datos que ingresa el usuario, o sea que para un mismo nodo se tienen varios valores y paths (p.e. para DvQuantity se necesita magnitude y units).
        * Se cumplen todas las restricciones del propio RM (como que todo element debe tener value o nullflavor, y que tenga value quiere decir que todos los valores para crear el DataValue fueron ingresados por el usuario).

  * El procedimiento de verificación podría ser:
    * Como entrada tengo un mapping de path->dato (posiblemente con múltiples ocurrencias de dato para la misma path).
    * Por la path del dato, pido las restricciones que aplican.
    * Las restricciones sobre valores, son verificables de forma directa.
    * Las restricciones sobre completitud de datos estructurados, pueden requerir lookup sobre el mapping de datos, para obtener los demás valores necesarios para verificar las restricciones (ver que no genero el nodo del RM, solo verifico restricciones).
    * Las restricciones sobre ocurrencias deben ser verificadas sin tomar las paths de los datos que vienen porque un valor puede no venir y ser de un nodo obligatorio:
      * Si un nodo ancestro es obligatorio y no viene ningún valor para él, o si no vienen todos los valores necesarios para crear sus nodos descendientes, se debe reportar el error.
      * El caso general del anterior es cuando la ocurrencia mínima es un número mayor que uno, se debe hacer una verificación similar, solo que para múltiples nodos.
      * Si un nodo ancestro es eliminado, hay que verificar que no venga un valor para él o sus descendientes.
    * Las restricciones sobre multiplicidad de colecciones, se deben verificar sobre la cantidad de nodos hijos que se crearían si hubiera binding.
      * Esto sería mucho más fácil si se hiciera el binding porque es simplemente ver cuantos hijos generé y ver si hay más o menos de los que esperaba para la colección.
      * Como no se va a hacer el binding, en el momento que se hacen las verificaciones anteriores, se debería ir contando cuantos nodos de cada tipo crearía si hiciera binding, para cada nodo con atributos de tipo colección.
      * En realidad esto es un pseudo binding, porque tengo que ver para cada path del arquetipo, desde la raíz, todos los atributos múltiples (p.e. observation.data.events, itemtree.items, cluster.items, etc) cuántas paths de datos que me vienen hay para cada uno, y de esas, contar solo las paths que generarían nodos hijos distintos (p.e. pueden haber 2 paths para obs.data.events, pero las 2 ser para un mismo element que va a tener a un solo event como ancestro).
        * Todas las rutas de atributos de tipo colección pueden estar precalculados, para saber qué verificar para cada arquetipo.
          * Se puede saber entonces si para un determinado atributo de tipo colección tiene una cardinalidad > 0, y es ahí donde tengo que verificar la restricción, si no se da ese caso, no es necesaria la verificación.
        * Se puede hacer un servicio que haga binding temporal solo con el objeto de unir paths para los mismos nodos y detectar realmente cuantas paths para distintos nodos hay, o sea, cuantos nodos distintos hay.
        * Otra alternativa es no darle importancia a la validación de cardinalidad y no validar. En general esto va a andar bien si no se van a requerir cardinalidades obligatorias a nivel de arquetipos, si hubiera algún arquetipo con un atributo de cardinalidad > 0, habría que resolverlo de alguna forma.








#### Generación de pantalla de edición ####
#### Generación de pantalla de visualización ####
#### Capacidad de hacer búsquedas ####
#### Capacidad de generar XMLs del RM ####


### Ideas para un modelo plano útil para implementar consultas EQL: ###

Esta discusión deja claros varios puntos de implementación respecto a AQL/EQL: http://old.nabble.com/AQL-parser-in-Java--%2B-AQL-response-format-%28using-the-AS-operator%29-td19738736.html

El modelo plano puede servir para sustituir al estructurado (se podría elegir usar uno u otro), o también como índice del modelo estructurado, que se crea cuando el registro ya está cerrado (esto evita tener que cambiar GUIGen y DataBinder, y también permite hacer consultas, pero siguen habiendo problemas de performance en el binder para estructuras grandes).

Un modelo plano que permita reconstruir una estructura del RM, debería tener por lo menos los cambios:
  * Archetype ID (define el tipo del RM: Observation, Action, Composition, etc)
  * Path

Se debe notar que para nodos hoja estructurados (como DvQuantity(magnitude,units)) que además son multivaluados, se tendrán varias Paths para el mismo nodo (una para magnitude y otra para units), y además se tendrán Paths repetidas por ser multivaluado. El problema a resolver es, en caso de tener 2 instancias del nodo, como indicar que par de Paths corresponden a una instancia del nodo y que par de Paths a la otra instancia. Esto se puede resolver agregando un atributo:
  * Instance Node ID (para diferenciar entre nodos hermanos, es decir que tienen la misma path).

Entonces, para un contenedor C de múltiples instancias de un nodo con path P y tipo DvQuantity, se tendrían las siguientes paths:
```
  * P[0]/units
  * P[0]/magnitude
  * P[1]/units
  * P[1]/magnitude
```

Donde 0 y 1 son los identificadores de instancia de cada DvQuantity contenido en C.

**A tener en cuenta**
Si se consideran los identificadores de instancia, las rutas del RM no serían iguales que las rutas del AOM, porque en los arquetipos no hay instancias.
Por otro lado, removiendo los identificadores de instancia, las rutas de nodos hermanos quedarían iguales entre sí, y iguales a las rutas en los arquetipos:
```
  * P/units
  * P/magnitude
```

También se pueden tener nodos "primos", es decir que el nodo padre de un nodo hoja pueda ser múltiple, entonces tengo que saber "cuál es mi padre", y diferenciarlo de mi "tío" (gemelo de mi padre), para esto tendría un atributo:
  * Parent ID

Este caso sería similar al anterior, solo que el contenedor C es el que puede ser múltiple. Si PC es la path a C, y C solo puede contener un DvQuantity en su campo "value", se tendría:
```
  * PC[0]/value/units
  * PC[0]/value/magnitude
  * PC[1]/value/units
  * PC[1]/value/magnitude
```

En todo momento, cada "value" de tipo DvQuantity, debe saber a que instancia del contenedor C pertenece, si a la que tiene identificador de instancia 0 o 1.

Aquí también pasa que si se quitan los identificadores de instancia, las paths para los nodos hermanos son iguales (y para todos sus hijos, o sea los nodos primos). Y éstas además quedan iguales a las paths definidas en el arquetipo.


Luego, si quisiera todas las instancias de los nodos generados para un determinado arquetipo, debería poder diferenciar estructuras enteras. Todos las instancias de nodos de una misma estructura, deberían tener una misma referencia. Para esto se tendría un atributo:
  * Structure ID

Si se tuviera otra estructura donde estuvieran todos los atributos útiles para el filtrado y la búsqueda e registros, indizados por este Structure ID, se podrían cargar desde datos complejos (documentos), hasta datos simples (elementos) filtrando o buscando. Algunos de los campos de búsqueda y filtrado se muestran más abajo, pero en resumen serían:
  * Datos del paciente (nombre, edad, sexo, identificadores, etc)
  * Datos del responsable de la atención (nombre, edad, sexo, identificadores, etc)
  * Datos del autor del registro (nombre, edad, sexo, identificadores, etc)
  * Datos temporales: apertura y cierre del registro
  * Datos del lugar físico/servicio: hospital, departamento, punto de atención
  * Metadatos como tags


Debido a que un mismo arquetipo puede ser utilizado en distintos templates de distintas formas, también debería saber que template se usó para una estructura, así que cada nodo tendría un campo:
  * Template ID

Nota: según la especificación de openEHR los únicos nodos que tienen referencias a templates son los raíces de arquetipos.

Si quisiera todas las estructuras para un determinado paciente, y además tener un nivel de indirección en la identificación, tendría un campo codificado con un algoritmo one-way como MD5, que se calculara a partir del UIDBasedID con su tipo y número. De esta forma, la única manera de saber a quien corresponde el registro es que un usuario habilitado acceda al IMP y calcule el MD5 a partir de un ID de paciente, y use ese MD5 para hacer consultas en la HCE:
  * Party ID

Como siempre se necesitan hacer consultas por fecha, agregaría dos períodos, uno para indicar cuando se llevó a cabo el acto, y otro para indicar cuando se hizo el registro del acto, la HCE le dará los valores correctos a estos campos según la semántica definida, por ejemplo si las dos fechas de cuando se hizo el acto coinciden, indica un acto puntual.
```
  * effective time (Interval<DateTime>)
  * record time (Interval<DateTime>)
```

El resto de la información es propia de los datatypes del modelo y de las estructuras de datos (Cluser, Element, List, Table, etc). Se podría tener un registro genérico con los campos descritos arriba (como metadata para queries), y registros particulares para los distintos tipos de datos del RM (pero con un modelo plano basado en FKs).


### Referencias sobre AQL/EQL: ###

  * http://www.openehr.org/wiki/display/spec/AQL+Operators


### Ideas sobre un modelo de información mínimo ###

Sabiendo que muchos niveles de la estructura que conforma un registro clínico completo, que cumple con el modelo de referencia de openEHR, es derivado en gran medida a partir de los arquetipos, y con pocos o ningún dato ingresado por el usuario o por el sistema, cabe preguntarse: ¿porqué debe generarse una estructura completa al hacer DataBinding? Es decir, ¿porqué generar una estructura completa y persistirla físicamente, si gran parte de esta puede ser total o parcialmente generada con el procesamiento de arquetipos?

La propuesta es explorar la idea de plantear un modelo de persistencia mínimo necesario para guardar toda la información, tal que con la estructura mínima y los arquetipos correspondientes, se pueda generar una estructura completa cuando sea requerido. Si este modelo existiera, el proceso de DataBinding sería más simple y más performante.

Para hacer el análisis, es necesario analizar estructuras completas, identificar cuáles datos fueron ingresados por el usuario o por el sistema, e identificar todo lo demás que fue derivado de un conjunto de arquetipos. A simple vista, parece que con un nivel de CLUSTERs y un nivel de ELEMENTs, se tendría un 90% de toda la información clínica ingresada por el usuario. Luego habría que analizar cómo asociar información demográfica, y por último, ver donde va toda la información relacionada con el documento clínico (COMPOSITION) como fechas, descripciones, firma del responsable, lugares físicos y servicios asociados, etc, etc.

En última instancia, una estructura completa podría ser modelada mediante arquetipos, modelo persistente mínimo y metadatos (implementados dentro o fuera del modelo persistente, pero que no forman parte del modelo en sí, si no que son de uso exclusivo para la reconstrucción de estructuras).

Relevamiento de datos útiles y donde van en las estructuras del modelo de información: http://code.google.com/p/open-ehr-gen-framework/wiki/EstructurasDeDatos


## Data Binding ##

El data binding a un modelo con estructura de árbol es muy costoso. De aplanarse el modelo como se comenta en el punto anterior, el componente de data binding debe ser modificado. Con un modelo plano el data binding es directo, el único punto de complejidad a tener en cuenta es la validación de datos. La validación hoy está implementada como parte del componente data binder, que al encontrar los nodos "hoja" del árbol definido por el arquetipo, obtiene las restricciones de ese nodo y valida los datos. Con una estructura plana (no se recorre el árbol) deberá ser necesario pedirle al arquetipo explícitamente cada restricción para poder chequearla contra el dato a validar. Lo más complejo es encontrar restricciones de cardinalidad equivalentes, por ejemplo:

  * Se tiene una list con 2 elementos
  * La list tiene restricción de cardinalidad 0..n
  * Un elemento tiene ocurrencia 1..n
  * El otro elemento tiene ocurrencia 1..n

Entonces la lista debería tener cardinalidad mínima 2, y lanzar un error si tiene menos de un elemento.


## Generación de GUI ##

En lugar de regenerar la GUI cada vez, ya que se genera en base a recorrer el árbol definido en el arquetipo, se podría:

  * Cachear GUIs: guardar el HTML generado y mostrar ese (p.e. si estoy editando, debería tener un mecanismo de cargarle datos a ese HTML estático, p.e. con JS).
    * Cachear GUIs para el ingreso de datos.
      * Cuando el sistema arranca, se cachearían todas las interfaces para el ingreso de datos, luego se bajan las que se utilizan menos, y se dan de alta de nuevo en el caché cuando son utilizadas.
    * Cachear GUIs para mostrar datos ingresados.
      * Una vez que se ingresa un registro, la visualización de dicho registro se debería cachear, para que las visualizaciones posteriores sean más rápidas.
      * Se tendría un conjunto de reglas para saber cuando dar de alta o baja la GUI del caché, por ejemplo si hace mucho tiempo que no se visualiza, se dará de baja, y si el paciente tiene una cita mañana, hoy se comienza el caching de sus registos pasados para visualización.
  * Generar la GUI en base a una estructura plana derivada del arquetipo, es decir, transformar el arquetipo original estructurado en uno plano para su uso directo. Este tipo de arquetipo plano también serviría para obtener las restricciones directamente de validación para cada nodo hoja (se menciona en el punto anterior).


**Estoy recibiendo altos tiempos de ejecución en el GUIGen del show**

Esto puede pasar porque carga los templates de disco y son muchos templates con poco código. Tal vez en lugar de hacerlo tan recursivo como ahora, lo mejor sería hacer grandes templates p.e. uno por cada tipo de entry a mostrar.


# Caching para visualización de registros clínicos #

Por su naturaleza, los registros clínicos son utilizados más en modo "solo lectura", por su naturaleza documental/histórica. Por lo tanto, puede servir concentrarse en acelerar la parte de visualización de registros.

Hoy el sistema trae de la base de datos cada registro del paciente, cada vez que un usuario desea verlo. Sería interesante ver cómo disminuir las transacciones a la base, tal vez con un cacheo en memoria.

El tamaño de este caché podría ser configurable (para adaptarlo a la infraestructura de hardware disponible), y podría tener distintos modos de ejecución, p.e. "online" y "offline". En el modo "online", los registros que son referenciados, se cargan la primera vez desde la base y se ponen en caché, luego por alguna criterio de tiempo, envejecimiento o cantidad de hits, se borraría del caché. Mientras esté en caché, no se consultaría a la base de datos. En el modo "offline", podrían haber procesos por lotes que corren cada noche, y cachean todos los documentos que interesan (p.e. los 10 últimos) para los pacientes que tendrán una consulta el día de mañana (habría que tener algún tipo de integración con Agenda y el Demográfico). De esta forma, muchos de los registros clínicos a cargarse en las próximas consultas médicas, tendrán gran cantidad de hits, aliviando la carga en la base de datos.

Incluso, no solo las estructuras de datos podrían cachearse, sino que podrían cachearse las vistas, que también serían estáticas debido a que los datos son estáticos. Incluso, otras opciones serían generar CDAs, o PDFs, y cachear eso en lugar de la estructura de datos (u otra opción útil para mostrar datos, y que utilice la menor memoria posible, p.e. un string JSON).

En el caso del string JSON, se podría hacer un GuiGen Javascript, que funcione del lado del usuario, por lo que se disminuye la carga en servidor, y ser aceleran los tiempos de render. En este caso, tal vez se necesitaría también una representación JSON de los arquetipos y templates involucrados.


# Compresion de respuestas del servidor #

Existen algunos plugins de Grails que permiten que las respuestas del servidor sea comprimidas:

  * http://www.grails.org/plugin/ui-performance
  * http://refactor.com.au/blog/grails-ui-performance-plugin-beware-uiperformancehtmlcompress
  * http://jira.grails.org/browse/GRAILS-4183