/**
 * 
 */
package demographic.role

import demographic.party.*
import authorization.Permit

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 * 
 * Es necesario que Role decienda de Party para poder decir que un rol hizo algo
 * y saber que persona lo hizo a traves de su performer.
 * 
 * Una instancia por cada rol.
 */
class Role extends Party {
    
    // Roles predefinidos
    // FIXME: deberian ir en una tabla maestra
    static String GODLIKE        = 'master_of_the_universe' // rol de prueba con acceso a todo
    static String ADMIN          = 'administrador'  // administrador del sistema con acceso al backend
    static String PACIENTE       = 'paciente'       // este rol no tiene acceso al sistema
    static String MEDICO         = 'medico'         // acceso al sistema con capacidad de registro clinico y demografico
    static String ENFERMERIA     = 'enfermeria'     // acceso al sistema con capacidad de registro clinico y demografico
    static String ADMINISTRATIVO = 'administrativo' // acceso al sistema con capacidad de registro demografico
    
    // Intervalo de validez del rol: en el modelo de OpenEHR es un Interval<DvDate>
    /*
     * se pone en RoleValidity porque rol es una instancia por cada tipo de rol, y la validez es por cada instancia de persona.
     * 
    Date timeValidityFrom
    Date timeValidityTo
    
    // Actor que tiene este rol asignado
    Actor performer
    */
    
    // Esto se cambia por Permit
    //static hasMany = [capabilities:Capability]
    static hasMany = [permits: Permit]

    static constraints = {
        //timeValidityTo(nullable:true)
       type(nullable:false, blank:false)
    }
    
    public String toString()
    {
       return this.type
    }
}