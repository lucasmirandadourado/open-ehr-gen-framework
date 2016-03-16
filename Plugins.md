# Introducción #

Los sistemas de Historia Clínica Electrónica son sistemas complejos, formados por múltiples componentes, subsistemas, funcionalidades, tecnologías y estándares.

El objetivo de esta página es agrupar las ideas que surjan sobre la integración de distintos componentes de una forma organizada, independiente (bajo acoplamiento), y que agregue valor al sistema resultando (alta cohesión).


# Detalles #

API para conexión de plugins:
  * Es necesario definir algún tipo de interfaz en el core del framework para que los plugins se puedan comunicar con el framework.
  * Cada plugin debe a su vez cumplir con una interfaz definida, para que el core del framework pueda también enviarle información al plugin.
  * Debe implementarse un módulo de gestión de plugins, desde donde un usuario con permisos, puede instalar, desinstalar, activar, desactivar, actualizar, configurar, etc, etc, etc, los plugins.
  * Pueden haber plugins que un usuario ve y otro no, es decir que los plugins pueden instalarse para todos los usuarios, para algunos roles, o para usuarios individuales.


# Tipos de plugins #

  * Servicios
    * Provee de comunicación con sistemas externos.
    * Agrega interfaces para que otros sistemas accedan a la HCE implementada con el framework.
  * Funcionalidad
    * ...
  * GUI
    * Widgets/aplicaciones complementarias
    * Diseño/tema/posición de items/organización de menúes
  * Datos
    * Integración de fuentes de datos externas
  * Verticales (incluyen varios de los anteriores)
    * CPOE: Prescripción de medicamentos
      * Integración con Vademecum local
      * Ver RxNorm de EEUU: http://www.nlm.nih.gov/research/umls/rxnorm/
      * Soporte para codificaciones locales
      * Mapeo a codificaciones internacionales
      * Control completo de la prescripción (períodos de tiempo, cantidades, dosis, repeticiones, etc)
      * Reglas para detectar conflictos/interacciones y lanzar alertas: droga-droga, droga-alimento, droga-problema de salud
    * Solicitud de estudios de laboratorio
      * Puede ser solo la orden o (p.e. en emergencia) la orden va con una muestra que se saca en el lugar de la atención. En este caso habría que pensar en algo que pudiera imprimir los datos patronímicos de la persona en etiquetas adhesivas que se pegarán en las muestras, y analizar la posibilidad de imprimir códigos de barras con la identificación del paciente.
      * También se espera la integración desde el laboratorio donde se reportan los resultados, y estos deben mostrarse en la HCE (posiblemente junto a la orden).
    * Solicitud de estudios imagenológicos
      * Debe estar la solicitud del estudio a radiología y la obtención del resultado (notificación desde radiología).
      * Puede incluir la visualización de contenido almacenado en un PACS o RIS (imágenes, videos, informe radiológico).


## Integración de plugins en la GUI ##

Sería bueno definir ciertas zonas en la GUI, que puedan ser usadas para mostrar distintos elementos de los plugins.

Por ejemplo, si un plugin genera una vista para registro clínico, en el menú debería aparecer un item para acceder a ese registro.

Otra idea es que haya una zona de "widgets" que el usuario pueda ver, por ejemplo si un médico en ambulatorio está en su "escritorio" dentro del sistema, podría ver la lista de pacientes a atender en un widget, en otro podría ver cuáles tenían estudios pendientes, etc, etc. Dentro del registro clínico, podría ver resultados de estudios en un widget.

El tema de los widgets es similar a lo que tiene implementado iGoogle: http://www.google.es/ig