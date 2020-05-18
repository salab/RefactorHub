package jp.ac.titech.cs.se.refactorhub.services

import jp.ac.titech.cs.se.refactorhub.exceptions.BadRequestException
import jp.ac.titech.cs.se.refactorhub.exceptions.ForbiddenException
import jp.ac.titech.cs.se.refactorhub.exceptions.NotFoundException
import jp.ac.titech.cs.se.refactorhub.models.Draft
import jp.ac.titech.cs.se.refactorhub.models.Refactoring
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

    fun fork(parent: Refactoring) = draftRepository.save(
        Draft(
            userService.me(),
            parent.commit,
            parent,
            null,
            parent.type,
            parent.data,
            parent.description
        )
    )

    fun edit(origin: Refactoring): Draft {
        val me = userService.me()
        if (origin.owner.id != me.id) throw ForbiddenException("User(id=${me.id}) is not Refactoring(id=${origin.id})'s owner.")
        return draftRepository.save(
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
    }

    fun save(draft: Draft) = draftRepository.save(draft)

    fun discard(id: Long) = delete(getByOwner(id).id)

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

    private fun removeEmptyElements(types: Map<String, Element.Info>, elements: MutableMap<String, Element.Data>) {
        types.entries.forEach {
            if (elements[it.key]?.elements?.isEmpty() == true) elements.remove(it.key)
        }
    }

    private fun updateElements(types: Map<String, Element.Info>, elements: MutableMap<String, Element.Data>) {
        types.entries.forEach {
            if (elements[it.key]?.type != it.value.type)
                elements[it.key] = Element.Data(it.value.type, it.value.multiple)
        }
    }

    fun updateElement(
        id: Long,
        category: String,
        key: String,
        index: Int,
        element: Element
    ): Draft {
        val draft = getByOwner(id)
        val map = when (category) {
            "before" -> draft.data.before
            "after" -> draft.data.after
            else -> throw BadRequestException("'category' need to be either 'before' or 'after'")
        }
        if (map.containsKey(key)) {
            try {
                map[key]!!.elements[index] = element
            } catch (e: IndexOutOfBoundsException) {
                throw BadRequestException("Draft(id=$id).data.$category[$key] doesn't have index=$index")
            }
        } else {
            throw BadRequestException("Draft(id=$id).data.$category doesn't have key=$key")
        }
        return save(draft)
    }

    fun addElement(
        id: Long,
        category: String,
        key: String
    ): Draft {
        val draft = getByOwner(id)
        val map = when (category) {
            "before" -> draft.data.before
            "after" -> draft.data.after
            else -> throw BadRequestException("'category' need to be either 'before' or 'after'")
        }
        if (!map.containsKey(key)) {
            throw BadRequestException("Draft(id=$id).data.$category doesn't have key=$key")
        }
        if (!map[key]!!.multiple) {
            throw BadRequestException("Draft(id=$id).data.$category[$key] doesn't have multiple elements")
        }
        map[key]!!.elements.add(map[key]!!.type.dataClass.createInstance())
        return save(draft)
    }

    fun deleteElement(
        id: Long,
        category: String,
        key: String,
        index: Int
    ): Draft {
        val draft = getByOwner(id)
        val map = when (category) {
            "before" -> draft.data.before
            "after" -> draft.data.after
            else -> throw BadRequestException("'category' need to be either 'before' or 'after'")
        }
        if (!map.containsKey(key)) {
            throw BadRequestException("Draft(id=$id).data.$category doesn't have key=$key")
        }
        if (!map[key]!!.multiple) {
            throw BadRequestException("Draft(id=$id).data.$category[$key] doesn't have multiple elements")
        }
        if (map[key]!!.elements.size <= index) {
            throw BadRequestException("Draft(id=$id).data.$category[$key] doesn't have index=$index")
        }
        map[key]!!.elements.removeAt(index)
        return save(draft)
    }

    fun addElementKey(
        id: Long,
        category: String,
        key: String,
        typeName: String,
        multiple: Boolean
    ): Draft {
        val draft = getByOwner(id)
        val map = when (category) {
            "before" -> draft.data.before
            "after" -> draft.data.after
            else -> throw BadRequestException("need to be either 'before' or 'after'")
        }
        val type = try {
            Element.Type.valueOf(typeName)
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("type=$typeName is unsupported")
        }
        if (!map.containsKey(key)) {
            map[key] = Element.Data(type, multiple)
        } else {
            throw BadRequestException("Draft(id=$id).data.$category already has key=$key")
        }
        return save(draft)
    }

    fun deleteElementKey(
        id: Long,
        category: String,
        key: String
    ): Draft {
        val draft = getByOwner(id)
        val map = when (category) {
            "before" -> draft.data.before
            "after" -> draft.data.after
            else -> throw BadRequestException("need to be either 'before' or 'after'")
        }
        if (!map.containsKey(key)) {
            throw BadRequestException("Draft(id=$id).data.$category doesn't have key=$key")
        }
        val type = when (category) {
            "before" -> draft.type.before
            "after" -> draft.type.after
            else -> throw BadRequestException("need to be either 'before' or 'after'")
        }
        if (type.containsKey(key)) {
            throw BadRequestException("Draft(id=$id).type.$category should have key=$key")
        }
        map.remove(key)
        return save(draft)
    }

}
