/**
 * 
 */

import java.util.Locale;

import org.openehr.am.archetype.Archetype;

import support.identification.TerminologyID;
import archetype_repository.ArchetypeManager

import grails.converters.*

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 *
 */
class ArchetypeManagerController {
    
    def index = {
        redirect(action:'list')
    }
    
    def list = {
            
        def manager = ArchetypeManager.getInstance()
        def archetypes = manager.getLoadedArchetypes()
        def actualizaciones = manager.getLastUse()
        
        //println "Arquetipos: " + archetypes.values()
        
        return [archetypeMap: archetypes, lastUseList: actualizaciones]
    }
    
    def unloadAll = {
        def manager = ArchetypeManager.getInstance()
        manager.unloadAll()
        redirect(action:'list')
    }
    
    /*
    def unload = {
            
        def manager = TemplateManager.getInstance()
        manager.unload(params.id)
        
        println "UNLOAD: " + params.id
        
        redirect(action:'list')
    }
    */
    
    /**
     * Consultas semanticas:
     * 
     * 1. Ingreso:
     *  - http://localhost:8080/ehr/archetypeManager/query
     * 
     * 2. Selecciona concepto:
     *  - http://localhost:8080/ehr/archetypeManager/query?archetypeId=openEHR-EHR-EVALUATION.triage_trauma.v2&conceptName=Triage+de+trauma
     * 
     * 3. Selecciona paths: (POST)
     *  - params:
           [ paths: [/data[at0001]/items[at0002], /data[at0001]/items[at0008]],
             fromDate:, archetypeId:openEHR-EHR-EVALUATION.triage_trauma.v2,
             doit:Seleccionar paths,
             conceptName:Triage de trauma,
             _paths:[, , , , ],
             action:query,
             controller:archetypeManager ]
     *
     * 4. Agregacion para los tipos soportados (DvOrdinal, CodePhrase) (estos definen listas de valores por eso es facil agrupar)
     *  - Para agrupar fechas y numeros (DvCount) es necesario definir rangos (hay que dejar que el usuario los defina)
     *  - Para DvQuantity tambien hay que definir rangos para la magnitud, pero hay que asegurar que la unidad es la misma.
     *  - Para agrupar texto libre se podrian definir terminos a buscar en el texto, y si esta en el texto se cuenta 1 (hay que dejar que el usuario ponga la lista de valores a buscar)
     *  - TODO: los boolean son faciles de agrupar tambien como DvOrdinal y CodePhrase.
     *  - Para DvCodedText (CodePhrase) donde la restriccion no es list sino que es un bind con una terminologia se
     *    puede hacer una agregacion igual, pero los valores posibles son muchos mas, lo optimo seria agrupar por
     *    los valores en la db y no por los valores en la terminologia (voy a tener muchos ceros). Para saber los
     *    valores posibles es hacer una query a la db con distinct, incluso con eso ya puedo hacer el count agrupando (group by).
     *  - otros: DvIdentifier, DvURI, DvProportion
     */
    
    /**
     * Test de la idea de buscar datos en el registro del paciente, basado en arquetipos y paths.
     */
    def query = {
       
       println params
       
       /**
        * Ideas:
        * - Pueden haber distintas versiones del mismo arquetipo, dar 2 opciones:
        *   - seleccionar el concepto, para cualquier version
        *   - seleccionar el concepto y una version particular de arquetipo 
        */
       
       // Map id arquetipo -> nombre concepto
       def clinicalConcepts = [:]
       
       // Arquetipo seleccionado desde la lista de conceptos
       def archetype
       
       // Lista de elementos del RM al filtrar por arquetipo y path
       def data
       
       // parentCompositionId -> [Pathable]
       def dataByComposition = [:]
       
       // Arquetipos
       def archs = archetype_repository.ArchetypeManager.getInstance().getLoadedArchetypes()
       
       // Lista de paths seleccionadas dentro de un arqutetipo
       List paths
       
       
       // El primer paso es cuando entra y se le muestran los conceptos, ahi debera seleccionar uno
       if (!params.archetypeId)
       {
          // Nombre de concepto de cada arquetipo en la KB (id at0000)
          def terms = binding.CtrlTerminologia.getInstance()
          def terminologyId = TerminologyID.create('local', null) // Para sacar el nombre del concepto de la ontologia del arquetipo
          
          // archetypeId => archetype
          archs.each { entry ->
             
             archetype = entry.value
             
             def conceptName = terms.getTermino(terminologyId, 'at0000', archetype, session.locale) // at0000 es el id de la raiz del arquetipo
             
             // archetypeId => conceptName
             clinicalConcepts[entry.key] = conceptName
          }
          
          archetype = null // Se reutiliza esta variable aca para la recorrida, la tengo que borrar para no generar problemas luego.
       }
       else
       {
          /**
          *  Si ya selecciono el concepto, le muestro las paths en el arquetipo (las paths van a
          *  depender de la version del arquetipo! (ahora considero que se consulta por arquetipos
          *  separados, no por concepto clinico)
          */
         
          archetype = archs[params.archetypeId]
         
          // Para mostrar el concepto seleccionado
          clinicalConcepts[params.archetypeId] = params.conceptName
          
          /**
           * Idea: poder seleccionar mas de una path
           */
          if (params.paths) // Puede ser un string o un array
          {
             // Selecciono una path de un arquetipo, busco los datos con ese filtro
             
             // TODO: deberia filtrarse para el paciente actual, si es que hay uno seleccionado,
             // sino no tienen mucho sentido las consultas porque se necesitaria una funcion de agrecacion.
             
             // TODO: Tambien filtrar por fecha del registro contenedor inicio/fin
             //       en lugar de tener estos datos en todas las clases del RM para filtrar facil,
             //       podria tener una relacion fisica con el contenedor ppal, la composition.
             //       Y usar estas queries de hibernate que navegan relaciones para hacer el filtro
             //       sobre los valores de fecha de la composition. Si necesitaria que el valor en
             //       la composition sea plano, no un datatype, asi la consulta por fecha es simple.
             
             //println params
             
             // TODO: data deberia ser MAP<parentCompositionId, List<Pathable>>
             // asi por cada composition tengo todos los elementos de esa composition seleccionados por su path para ser mostrados
             // la composition le da marco temporal a esos datos, asi que es mas facils si quiero filtrar por fechas
             // el problema es que sin tener la relacion fisica entre Pathable y Composition, no puedo meter el filtro
             // por fecha en la query, y si pongo esa relacion me da un error de backref al salvar la section porque
             // la compo tiene un hasmany a section y section tien el parendCompositionId por estar declarado en pathable,
             // el problema que es si composition tambien es pathable, el parentComposition en pathable modela un posible
             // link entre composition y composition tambien, no se si eso puede causar problemas.
             
             
             // Haciendo que siempre reciba una lista aunque haya una sola path:
             // http://grails.org/doc/latest/guide/single.html#6.1.12%20Simple%20Type%20Converters
             paths = params.list('paths')
             
             println paths
             
//             if (params.paths.getClass().isArray())
//             {
                data = []
                def data1
//                params.paths.each{ pth ->
                paths.each{ pth ->
                   
                   // Necesario para comparar el dato codificado como XML en la base
                   def startTimeFilter = new data_types.quantity.date_time.DvDateTime(value: ((params.fromDate)?params.fromDate:'1900-01-01 00:00:00') )
                   com.thoughtworks.xstream.XStream xstream = new com.thoughtworks.xstream.XStream()
                   xstream.omitField(data_types.basic.DataValue.class, "errors");
                   def codedStartTimeFilter = xstream.toXML(startTimeFilter)
                   
                   //println pth
                   //println codedStartTimeFilter
                   
                   //data1 = hce.core.common.archetyped.Locatable.findAll( "FROM Locatable p WHERE p.archetypeDetails.archetypeId = ? AND p.path = ?", [params.archetypeId, pth, codedStartTimeFilter] )
                   
                   // El filtro funciona ok, el problema es que el restulado es una lista de pares ELEMENT/COMPOSITION,
                   // porque hace el producto cartesiando entre los Locatables y la Composition, y en realidad la composition la quiero solo para filtrar,
                   // no para el restulado final, talvez tengo que hacer una consulta anidada para composition.
                   //data1 = hce.core.common.archetyped.Locatable.findAll( "FROM Locatable p, Composition c WHERE p.archetypeDetails.archetypeId = ? AND p.path = ? AND c.context.codedStartTime < ?", [params.archetypeId, pth, codedStartTimeFilter] )
                   
                   // OK! usando nested query: http://www.felixgers.de/teaching/sql/sql_nested_queries.html
                   // Falta que pueda ingresar las fechas como filtro en la gui.
                   data1 = hce.core.common.archetyped.Locatable.findAll( "FROM Locatable p WHERE p.archetypeDetails.archetypeId = ? AND p.path = ? AND EXISTS( SELECT c.id FROM Composition c WHERE c.id = p.parentCompositionId AND c.context.codedStartTime > ?)",
                                                                         [params.archetypeId, pth, codedStartTimeFilter] )
                   
                   
                   /* Hasta cierto punto la consulta podria hacerse con withCriteria, no se como hacer la consulta anidada con el EXISTS para filtrar por fecha de la composition!
                   def test = hce.core.common.archetyped.Locatable.withCriteria {
                      
                      archetypeDetails {
                         eq('archetypeId', params.archetypeId)
                      }
                      eq('path', pth)
                   }
                   */
                   
                   //println data1
                   
                   // Aplano cada consulta parcial en la solucion final
                   // TODO: asociar los datos que son para las mismas parentCompositons
                   
                   data1.each {
                      
                      data << it
                      
                      
                      // TODO: faltaria el filtro por paciente, para asociar las distintas compositions
                      // Asocia cada instancia de Pathable a la composition a la que pertenece,
                      // asi si tengo varias paths, puedo mostrar asociados los objetos de la misma composition.
                      if (!dataByComposition[it.parentCompositionId]) dataByComposition[it.parentCompositionId] = []
                      dataByComposition[it.parentCompositionId] << it
                   }
                }
                
                //println "data: " + data // Elements
                //println "dataByComposition: " + dataByComposition // Composition -> Elements
                
//             }
//             else
//             {
//                // Necesario para comparar el dato codificado como XML en la base
//                def startTimeFilter = new data_types.quantity.date_time.DvDateTime(value: ((params.fromDate)?params.fromDate:'1900-01-01 00:00:00') )
//                com.thoughtworks.xstream.XStream xstream = new com.thoughtworks.xstream.XStream()
//                xstream.omitField(data_types.basic.DataValue.class, "errors");
//                def codedStartTimeFilter = xstream.toXML(startTimeFilter)
//                
//                // Esta consulta se podria hacer igual con withCriteria
//                data = hce.core.common.archetyped.Locatable.findAll( "FROM Locatable p WHERE p.archetypeDetails.archetypeId = ? AND p.path = ? AND EXISTS( SELECT c.id FROM Composition c WHERE c.id = p.parentCompositionId AND c.context.codedStartTime > ?)", [params.archetypeId, params.paths, codedStartTimeFilter] )
//                
//                data.each {
//                  if (!dataByComposition[it.parentCompositionId]) dataByComposition[it.parentCompositionId] = []
//                  dataByComposition[it.parentCompositionId] << it
//                }
//             }
             
             //println "dataByComposition:"
             //println dataByComposition
             
          }
       }
       
       return [clinicalConcepts: clinicalConcepts, archetype: archetype, paths: paths, data: data, dataByComposition: dataByComposition]
    }
    
    /**
     * archetypeId
     * chart_path del ELEMENT :: y del CDvOrdinal o CCodePhrase donde estan los valores para clasificar
     * TODO: dateFrom
     */
   def aggregate = {
       
      println params
      
      // Arquetipo seleccionado desde la lista de conceptos
      def archetype = archetype_repository.ArchetypeManager.getInstance().getArchetype(params.archetypeId)
       
      // Lista de elementos del RM al filtrar por arquetipo y path
      def data
       
       
      def elemPath = params.chart_path.split("::")[0]
      def constraintPath = params.chart_path.split("::")[1]
       
       
      def startTimeFilter = new data_types.quantity.date_time.DvDateTime(value: ((params.fromDate)?params.fromDate:'1900-01-01 00:00:00') )
      com.thoughtworks.xstream.XStream xstream = new com.thoughtworks.xstream.XStream()
      xstream.omitField(data_types.basic.DataValue.class, "errors");
      def codedStartTimeFilter = xstream.toXML(startTimeFilter)
       
      // Esta consulta se podria hacer igual con withCriteria
      data = hce.core.common.archetyped.Locatable.findAll( "FROM Locatable p WHERE p.archetypeDetails.archetypeId = ? AND p.path = ? AND EXISTS( SELECT c.id FROM Composition c WHERE c.id = p.parentCompositionId AND c.context.codedStartTime > ?)", [params.archetypeId, elemPath, codedStartTimeFilter] )
       

      // Valores para clasificar
      def constraint = archetype.node(constraintPath)
       
      // Codigos de agregacion -> valor agregado (counter)
      def aggregator = [:]
       
      // Codigos de agregacion -> nombres correspondientes (label para mostrar)
      def names = [:]
      def terms = binding.CtrlTerminologia.getInstance()
      def terminologyId = TerminologyID.create('local', null) // Para sacar el nombre del concepto de la ontologia del arquetipo
       
       
      // ============================================================================================
      // TODO: para cada tipo deberia haber agregadores distintos que se puedan seleccionar.
      //       ej. DvCount puedo querer SUM o AVG o MAX o MIN
      // ============================================================================================
      
      
      /**
       * TODO: que cada agregador por datatype se haga en una funcion aparte para no hacer if ... 
       */
      
      // FIXME:
      // DvQuantity tiene el problema de que depende de las unidades,
      // ej. hacer promedios de magnitudes con distintas unidades da cualquier cosa.
      // eso ahora no lo considero.
      if (constraint.rmTypeName == 'DvQuantity')
      {
		 // list de org.openehr.am.openehrprofile.datatypes.quantity.CDvQuantityItem
		 // Puedo usar las constraints para hacer promedios solo en la misma unidad
         println 'DvQuantity: ' + constraint.list
       
		 if (data.size() > 0)
         {
	        // Hay un solo agregador de promedio (los agregadores de los demas tipos
	        // lo que hacen es contar ocurrencias de un valor, eso para DvQuantity no
	        // tiene mucho sentido).
	        aggregator['promedio'] = 0
	       
	       
	        // Calculo de agregacion AVG en magnitude (sin considerar units distintas!!!)
			// data es List<Element>
	        data.each {
	         
	           println it.value.magnitude
	         
	           aggregator['promedio'] += it.value.magnitude
	        }
			 
            aggregator['promedio'] = aggregator['promedio'] / data.size()
	       
	        // labels (hay solo una porque es un promedio)
	        names['promedio'] = 'promedio' // TODO: I18N
         }
      }
      
      if (constraint.rmTypeName == 'DvOrdinal')
      {
         println 'DvOrdinal: ' + constraint.list // constraint.list lista de org.openehr.am.openehrprofile.datatypes.quantity.Ordinal
         
         // Inicializacion de valores de agregacion
         // Ordinal.symbol es CodePhrase, y .codeString tengo el codigo de la restriccion.
         constraint.list.each {
            
            aggregator[it.symbol.codeString] = 0
         }
         
         // Calculo de agregacion
         data.each {
            
           println it.value // DvOrdinal, .symbol es DvCodedText, .definingCode es CodePhrase
           
           aggregator[it.value.symbol.definingCode.codeString] ++
         }
         
         // labels
         aggregator.each {
            
            names[it.key] = terms.getTermino(terminologyId, it.key, archetype, session.locale) // at0000 es el id de la raiz del arquetipo
         }
      }
      
      if (constraint.rmTypeName == 'CodePhrase')
      {
         println 'CodePhrase: ' + constraint.codeList
         
         // Inicializacion de valores de agregacion
         constraint.codeList.each{
            
            aggregator[it] = 0
         }

         // Calculo de agregacion
         data.each {
            
           println it.value.definingCode.codeString
           
           aggregator[it.value.definingCode.codeString] ++
         }
         
         // labels
         aggregator.each {
            
            names[it.key] = terms.getTermino(terminologyId, it.key, archetype, session.locale) // at0000 es el id de la raiz del arquetipo
         }
      }
	  
      if (constraint.rmTypeName == 'DvBoolean')
      {
          // ***
          // El caso de null no es necesario porque cuando es null no se crea el Element, asi que no hay Element para verificar su valor null aqui...
          
          // Inicializacion de valores de agregacion
          aggregator['true'] = 0
          aggregator['false'] = 0
          //aggregator['null'] = 0 // No responde ***
          
          // Calculo de agregacion
          data.each {
            
            // it Element, it.value DvBoolean, it.value.value Boolean
            //println it.value.value
            aggregator[it.value.value.toString()] ++
          }
          
          // labels
          names['true'] = 'Si'
          names['false'] = 'No'
          //names['null'] = 'NR' ***
      }
       
      if (constraint.rmTypeName == 'DV_TEXT')
      {
          //println 'DV_TEXT'
          //println params.aggKeys // valores de agregacion puestos por el usuario, separados por coma
          def values = params.aggKeys.split(',')
          
          //println values
          
          values.each{ aggValue ->
            
            aggregator[aggValue] = 0
            names[aggValue] = aggValue // Uso el valor del usuario como label
          }
          
          //println aggregator
          
          // La agregacios es viendo por si el aggValue esta incluido en el DvText, si esta cuenta 1. 
          
          // Calculo de agregacion
          data.each {
            
             // it Element, it.value DvText, it.value.value String
             //println it.value.value

             values.each() { search ->
                
                // TODO: case insensitive
                // TODO: trim de espacios en search
                if (it.value.value.contains(search))
                {
                   println "match "+ it.value.value +" contains "+ search
                   aggregator[search] ++
                }
             }
          }
          
          //println aggregator
      }
       
       
       
       //println data
       //println aggregator // code -> cantidad (el codigo deberia usarlo para obtener el nombre segun el servicio terminologico y el locale seleccionado
       //println names
       
       // TODO: tengo que pasar aggregator y names como JSON
       
       //render "opa"
       /*
       render(contentType: "text/json") {
          names
       }
       */
       
       def r = [ names: names, aggregator: aggregator ]
       
       render r as JSON
    }
    
}