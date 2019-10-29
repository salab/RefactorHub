package jp.ac.titech.cs.se.refactorhub.services

import jp.ac.titech.cs.se.refactorhub.exceptions.BadRequestException
import jp.ac.titech.cs.se.refactorhub.exceptions.ForbiddenException
import jp.ac.titech.cs.se.refactorhub.exceptions.NotFoundException
import jp.ac.titech.cs.se.refactorhub.models.Draft
import jp.ac.titech.cs.se.refactorhub.models.Refactoring
import jp.ac.titech.cs.se.refactorhub.models.User
import jp.ac.titech.cs.se.refactorhub.models.element.Element
import jp.ac.titech.cs.se.refactorhub.repositories.DraftRepository
import org.springframework.stereotype.Service
import kotlin.reflect.full.createInstance

@Service
class DraftService(
    private val draftRepository: DraftRepository,
    private val userService: UserService,
    private val refactoringTypeService: RefactoringTypeService
) {

    fun get(id: Long): Draft {
        val draft = draftRepository.findById(id)
        if (draft.isPresent) return draft.get()
        throw NotFoundException("Draft(id=$id) is not found.")
    }

    fun getByOwner(id: Long): Draft {
        val draft = draftRepository.findById(id)
        val owner = userService.me()
        if (draft.isPresent) {
            return draft.get().also {
                if (it.owner != owner) throw ForbiddenException("User(id=${owner.id}) is not Draft(id=$id)'s owner.")
            }
        }
        throw NotFoundException("Draft(id=$id) is not found.")
    }

    fun create(origin: Refactoring) = draftRepository.save(
        Draft(
            origin.owner,
            origin.commit,
            origin.parent,
            origin,
            origin.type,
            origin.data,
            origin.description
        )
    )

    fun fork(owner: User, parent: Refactoring) = draftRepository.save(
        Draft(
            owner,
            parent.commit,
            parent,
            null,
            parent.type,
            parent.data,
            parent.description
        )
    )

    fun save(draft: Draft) = draftRepository.save(draft)

    fun delete(id: Long) = draftRepository.deleteById(id)

    fun update(
        id: Long,
        description: String? = null,
        typeName: String? = null
    ): Draft {
        val draft = getByOwner(id)
        if (description != null) draft.description = description
        if (typeName != null) {
            val type = try {
                refactoringTypeService.getByName(typeName)
            } catch (e: NotFoundException) {
                throw BadRequestException(e.message!!)
            }
            if (draft.type != type) {
                removeEmptyElements(draft.type.before, draft.data.before)
                removeEmptyElements(draft.type.after, draft.data.after)
                updateElements(type.before, draft.data.before)
                updateElements(type.after, draft.data.after)
                draft.type = type
            }
        }
        return save(draft)
    }

    private fun removeEmptyElements(types: Map<String, Element.Type>, elements: MutableMap<String, Element>) {
        types.entries.forEach {
            if (elements[it.key]?.incomplete == true) elements.remove(it.key)
        }
    }

    private fun updateElements(types: Map<String, Element.Type>, elements: MutableMap<String, Element>) {
        types.entries.forEach {
            if (elements[it.key]?.type?.name != it.value.name) elements[it.key] = it.value.dataClass.createInstance()
        }
    }

    fun updateBeforeElement(
        id: Long,
        key: String,
        element: Element
    ) = updateElement(id, key, "before", element)

    fun updateAfterElement(
        id: Long,
        key: String,
        element: Element
    ) = updateElement(id, key, "after", element)

    private fun updateElement(
        id: Long,
        key: String,
        which: String,
        element: Element
    ): Draft {
        val draft = getByOwner(id)
        val map = when (which) {
            "before" -> draft.data.before
            "after" -> draft.data.after
            else -> throw BadRequestException("need to be either 'before' or 'after'")
        }
        if (map.containsKey(key)) {
            map[key] = element
        } else {
            throw BadRequestException("Draft(id=$id).data.$which doesn't have key=$key")
        }
        return save(draft)
    }

}
