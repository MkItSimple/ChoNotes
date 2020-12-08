package cho.chonotes.business.domain.util

interface TwoEntityMapper <Entity, DomainModel>{

    fun mapFromEntity(entity: Entity): DomainModel

//    fun mapToEntity(domainModel: DomainModel): Entity
}