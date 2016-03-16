

# Introducción #

Los documentos clínicos que maneja el framework tienen una parte de datos clínicos y otra de datos demográficos/administrativos. Todo documento clínico es representado mediante la clase COMPOSITION del modelo de referencia del estándar openEHR.

El objetivo de esta página es describir la estructura interna de los documentos clínicos, modelados con openEHR, y al mismo tiempo clasificar las clases en "clases estructurales" y "clases de contenido".
Las clases estructurales serán las que solo sirven para representar estructura, y que pueden ser derivadas en cualquier momento desde un arquetipo, que podrían no necesitar persistencia explícita (porque pueden derivarse). Las clases de contenido, son las que realmente contienen datos clínicos y/o demográficos, las cuales necesitan una persistencia explícita.

El objetivo final, será simplificar el modelo, dejando solo las clases de contenido, más alguna metadata. Estos artefactos, más los arquetipos y templates, permitirán generar una estructura válida y completa del RM de openEHR, tomando la parte persistente (contenido), y derivando la parte estructural desde arquetipos y templates, y desde la metadata mencionada previamente.

Simplificar el modelo significa: 1. estructura de clases más simple (menos clases), 2. estructura de la db más simple (mejor performance), 3. proceso de data binding mucho más rápido y con menos código, 4. proceso de carga de datos mucho más rápido, 5. procesos de generación de GUI mucho más rápido (sobre todo cuando hay que mostrar datos cargados desde la base). En general la performance se vería mejorada, y se tendría menos código, ayudando a la mantenibilidad del framework.


# Datos básicos de cada documento clínico #
```
  * fecha de inicio: Composition.<EventContext>context.<DvDateTime>startTime
  * fecha de cierre: Composition.<EventContext>context.<DvDateTime>endTime
  * observaciones al ingreso: Composition.<EventContext>context.<ItemSingle>otherContext.<Element>item.<DvText>value.<String>value
```

## Datos internos de la composition ##
```
  * language: idioma en el que está el registro.
  * territory: país/región donde se hizo el registro
  * category: categoría del registro según vocabulario "composition category" de openEHR.
```

## Más datos del contexto de la composition ##
```
  * location: ubicación exacta donde se brinda el cuidado (sería el point of care).
  * setting: servicio donde se brinda el cuidado, según vocabulario "setting" de openEHR.
  * health_care_facility: The health care facility under whose care the event took place. This is the most specific workgroup or delivery unit within a care delivery enterprise that has an official identifier in the health system, and can be used to ensure medico-legal accountability.
```


# Información del paciente #
```
  * identificación del paciente:
    * ps = composition.<EventContext>context.participations
    * dentro de ps hay una Participation que su 'performer' es PartySelf, y que su función es 'subject of care'.
    * PartySelf.objectId: root/extension (tipo de identificador/identificador)
```

# Información del responsable de la atención #
```
  * Composition.<PartyIdentified>composer
```

# Estado del registro #

A medida que avanza el proceso de atención, el registro clínico pasa por distintos estados, que quedan almacenados en distintas instancias de la clase VERSION del modelo de información de openEHR.

Estados posibles:

  * STATE\_INCOMPLETE: registro incompleto.
  * STATE\_COMPLETE: registro completo (proceso clínico cerrado, y el paciente correctamente identificado).
  * STATE\_SIGNED: registro firmado.
  * Se guarda en version.lifecycleState

En Version.data se guarda la referencia a la Composition para la cual se define el estado, de esta forma, hay varias instancias de Version para la misma instancia de Composition. _Hoy se tiene solo una instancia de Version para una Composition, sobreescribiendo el estado, por lo que no se tiene la traza de cambios de estados. Esto se debe corregir._

# Información clínica #

```
Toda la información clínica está en Composition.<Collection<ContentItem>>content
```

**ContentItem:**
  * Clase abstracta, es el primer nivel de información clínica.
  * Patrón composite con subclases Section y Entry.
  * No almacena información, solo es estructura.

**Section:**
  * Subclase de CobntentItem.
  * No almacena información, solo es estructura.
    * Árbol de ContentItem.
  * Cluster alcanza para modelar la misma estructura.

**Entry:**
  * Clase hoja del composite de ContentItem
  * Guarda información:
    * language: indica el idioma en el que está escrito el contenido de la entrada. (a)
    * encoding: indica la codificación de caracteres del texto en la entrada. (a)
    * subject (no usado) (a)
    * provider (no usado) (a)
    * workflowId (no usado) (b)
    * otherParticipations (no usado)
  * Ninguna de esta información es ingresada por el médico, es toda sacada del contexto del sistema, entonces puede ser representado en otra estructura más alguna metadata que especifique a qué entry de qué arquetipo corresponde. La idea es que esta estructura sea simple de consultar.

(a) Para simplificar muchos aspectos, pero además porque es como se va a usar, todas las entradas de una misma composition tendrán los mismos: language, encoding, subject. Provider es un caso especial porque es quien provee la información en el registro. Si el paciente provee parte de la información (algunas entradas), o un médico, o la institución o un dispositivo médico, cada entrada debería reflejarlo. Pero si toda la información de una composition es provista por un solo agente, no sería necesario especificarlo por entrada.

(b) workflowId se utilizará para identificar el flujo de trabajo al cual el registro (entrada) está asociado, de forma de que se puedan ver todas las entradas que corresponden a un mismo flujo de trabajo, por ejemplo, ordenadas de forma cronológica. Además junto a los cambios de estado, y al enganche entre indicaciones/actividades y sus respectivas acciones, se podría tener una visión completa de la ejecución de un determinado flujo de trabajo. Un flujo podría ser un proceso clínico, una guía de práctica clínica o un algoritmo clínico.

El campo otherParticipations es como un comodín para especificar otros actores que participaron de la entrada (supongo que jugando algún rol en el proceso del cual se registró información). Creo que esto debería estar a nivel de composition solo, así se simplifica el nivel de entradas. Y si se desea especificar otras participaciones para entradas específicas, se podrá modelar otra composition y ahí se ponen las participaciones. Luego habrá varias formas de correspondencia entre las compositions, 1. serán para el mismo paciente, 2. pueden estar en el mismo marco de tiempo (p.e. mismo día), 3. probablemente tendrán el mismo workflowId, 4. puede ser que el médico responsable del registro sea el mismo.


## Subclases de ENTRY ##

Las subclases de ENTRY son básicamente contenedores de estructuras (ItemStructure y History) de datos clínicos. Más allá de los atributos de Entry antes mencionados, todo el nivel de estructuras se considera como "clases estructurales" que pueden ser derivadas desde el arquetipo, y por lo tanto, no se necesita persistencia explícita para esas clases.

### ADMIN\_ENTRY ###

Aparte de los campos heredados de ENTRY, ADMIN\_ENTRY solo contiene una estructura, por lo que si los campos de ENTRY son modelados en el modelo de datos mínimo, la clase ADMIN\_ENTRY no necesita estar presente, solamente se necesita su estructura interna.

### CARE\_ENTRY ###

Hay dos campos en la subclase CARE\_ENTRY para especificar un protocolo (protocol) y un identificador de guía (guideline\_id), que en conjunto con ENTRY.workflow\_id, permiten dar el contexto del proceso ejecutado.
  * El campo "protocol" indica cómo llega la información a la entrada. Para entradas INSTRUCTION, indica cómo se debe ejecutar la instrucción/orden. Este protocolo puede ser derivado de una guía clínica.
  * El campo "guideline\_id", opcionalmente, permite referenciar a la guía de la cual se derivó el "protocol".

El protocolo es una estructura arquetipable. _Entiendo que en el arquetipo se especifica un protocolo general, y que en el registro clínico, el médico/enfermera/técnico especifica cómo fue ejecutado ese protocolo. Por ejemplo, el protocolo para tomar la presión, puede indicar que el paciente debe estar acostado, sentado o parado, y en la ejecución se registra una de esas posiciones._ Si esto es cierto, el protocolo es parte del contenido clínico y debería estar en el modelo de información mínimo. De todas formas, es una estructura opcional, por lo que podría modelarse aparte y relacionarse de alguna forma con el modelo mínimo.


### Subclases de CARE\_ENTRY ###

Todas las subclases contienen, de forma directa o indirecta, estructuras de datos. Por otra parte, algunas contienen datos simples, como:
```
  * INSTRUCTION.narrative: DV_TEXT
  * INSTRUCTION.expirity_time: DV_DATE_TIME
  * INSTRUCTION.wf_definition: DV_PARSABLE
  * INSTRUCTION.activities: Collection<ACTIVITY>
    * description: ITEM_STRUCTURE
    * timing: DV_PARSABLE
    * action_archetype_id: String

  * ACTION.time: DV_DATE_TIME
  * ACTION.instruction_details: INSTRUCTION_DETAILS
    * instruction_id: LOCATABLE_REF
    * activity_id: String
    * wf_details: ITEM_STRUCTURE
  * ACTION.ism_transition: ISM_TRANSITION
    * current_state: DV_CODED_TEXT
    * transition: DV_CODED_TEXT
    * careflow_step: DV_CODED_TEXT
```

El problema que se presenta es cómo representar la colección de actividades de INSTRUCTION, y cómo se representa la transición y los detalles de la instrucción de ACTION.