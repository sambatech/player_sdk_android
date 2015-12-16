package com.sambatech.player.model;

import android.graphics.drawable.Drawable;

import java.lang.reflect.Field;

/**
 * @author tmiranda - 02/12/15
 */
public class SambaMedia {

	public String id;
	public String title = "";
	public String url;
	public String type = "";
	public Integer clientId;
	public Integer categoryId;
	public Project project;
	public String adUrl;
	public Drawable thumb;
	public int themeColor = 0xFF72BE44;

	@Override
	public String toString() {
		String desc = "";
		Field[] fields = getClass().getDeclaredFields();

		try {
			for (Field field : fields)
				desc += field.getName() + ": " + field.get(this) + '\n';
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return desc;
	}
}
