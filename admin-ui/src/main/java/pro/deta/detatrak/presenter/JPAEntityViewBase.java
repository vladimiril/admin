package pro.deta.detatrak.presenter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.deta.detatrak.event.EventBase;
import pro.deta.detatrak.util.EntityContainerHandler;
import pro.deta.detatrak.util.JPAUtils;
import ru.yar.vi.rm.model.NumberWrapper;

import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.PopupView;


public abstract class JPAEntityViewBase<E> extends EditViewBase implements EntityContainerHandler<E> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8600864478538693787L;
	public static final Logger logger = LoggerFactory.getLogger(JPAEntityViewBase.class);
	protected EntityItem<E> item = null;
	protected EntityContainer<E> container = null;
	protected Object itemId = null;
	protected BeanItem<E> addedBean = null;
	protected Class<E> type = null;
	protected FieldGroup binder;
//	private TableBuilder tableBuilder;
//	private ItemSetChangeEvent event;

	public JPAEntityViewBase(Class<E> e) {
//		this.container = JPAUtils.createCachingJPAContainer(e);
		type = e;
	}

	public void setItem(EntityItem<E> item,EntityContainer<E> container,PopupView view) {
		
	}

	public void setEntityContainer(EntityContainer<E> container) {
		this.container = container;
	}
	
	/**
	 * commented out due to not using.
	 * @param tb
	@SuppressWarnings("serial")
	public void setTableBuilder(TableBuilder tb) {
		this.tableBuilder = tb;
		event = new ItemSetChangeEvent() {
			@Override
			public Container getContainer() {
				return tableBuilder.getContainer();
			}
		};
	}
	
	 */
	@Override
	protected void buildUI(String parameter) {
		item = null;
		itemId = getItemId(parameter);
		form.removeAllComponents();
		
		binder = new FieldGroup();
		binder.setBuffered(false);
		if(itemId == null) {
			// если создаём новый объект - не надо его делать через JPA, в режиме Bean
			E bean = createBean();
			addedBean = new BeanItem<E>(bean);
			binder.setItemDataSource(addedBean);
			initForm(binder,bean);
		} else {
			item = container.getItem(itemId);
			binder.setItemDataSource(item);
			initForm(binder,item.getEntity());
		}
		
	}

	
	
	public E createBean() {
		try {
			return type.newInstance();
		} catch (InstantiationException e) {
			logger.error("Error while creating class instance " + type,e);
		} catch (IllegalAccessException e) {
			logger.error("Error while creating class instance " + type,e);
		}
		return null;
	}

	public Object getItemId(String parameter) {
		if(parameter != null && parameter.matches("^\\d+$")) {
			Integer currentId = !"".equals(parameter) ? new NumberWrapper(parameter).getInteger() : null;
			return currentId;
		} else if (!"".equalsIgnoreCase(parameter))
			return parameter;
		else
			return null;
	}

	abstract protected void initForm(FieldGroup binder,E bean);
	
	public void save() {
		try {
			binder.commit();
		} catch (CommitException e1) {
			logger.error("Error while binder.commit",e1);
		}
		if(item == null) {
			E e = addedBean.getBean();
			saveEntity(e);
			save(e);
//			postSaveEntity(e);
		} else {
			E e = item.getEntity();
			saveEntity(e);
			save(e);
//			postSaveEntity(e);
		}
	}
	
	private final void save(Object o) {
		JPAUtils.save(o);
		dispatchEvent(new EventBase("save"));
//		if(tableBuilder != null && tableBuilder.getTable()!= null) {
//			tableBuilder.getTable().containerItemSetChange(event);
//		}
	}

	public void saveEntity(E obj) {
		
	}
	

	
	public void cancel() {
		binder.discard();
		super.cancel();
		postCancel();
	}

	public void postCancel() {
		
	}
}
