package com.centerm.t5demolibrary.interfaces;

/**
 * 
 * @author JQJ
 * @category interface 数据读取中止判断回调
 *
 */
public interface EndOfRead
{
	boolean reach(byte[] buf, int len);
}
