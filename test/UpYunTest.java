

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.doumiao.joke.lang.UpYunHelper;
import com.doumiao.joke.schedule.UpYun;
import com.doumiao.joke.schedule.UpYun.FolderItem;

public class UpYunTest {

	public void uploadStatic() {
		String[] dirs = new String[] {
		// "D:/data/workspace/pri/java/joke/WebContent/static/js",
		"D:/data/workspace/pri/java/joke/WebContent/static/css"
		// "D:/data/workspace/pri/java/joke/WebContent/static/flash",
		// "D:/data/workspace/pri/java/joke/WebContent/static/images",
		// "D:/data/workspace/pri/java/joke/WebContent/static/avatar"
		};
		for (String d : dirs) {
			File df = new File(d);
			dir(df);
		}
	}

	public void dir(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				dir(f);
			}
		} else {
			try {
				String path = file.getPath().replace(
						"D:\\data\\workspace\\pri\\java\\joke\\WebContent\\",
						"");
				System.out.println(path);
				UpYun yun = UpYunHelper.getClient();
				yun.setContentMD5(UpYun.md5(file));
				boolean result = yun.writeFile(path, file, true);
				if (!result) {
					System.err.print("error");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void total() {
		UpYun client = UpYunHelper.getClient();
		long usage = client.getBucketUsage();
		System.out.println("空间总使用量：" + usage / 1024 / 1024 + "M");
	}

	@Test
	public void showFiles() {
		System.out.println("start show files");
		List<String[]> l = new ArrayList<String[]>();
		list("/article/0/2014/11", l, 2);
		for (String[] s : l) {
			System.out.println(s[1]);
		}
	}

	public void deleteFiles() {
		UpYun client = UpYunHelper.getClient();
		List<String[]> l = new ArrayList<String[]>();
		list("/article/0", l, 1);
		for (String[] s : l) {
			if (s[0].equals("d")) {
				client.rmDir(s[1]);
			} else {
				client.deleteFile(s[1]);
			}
		}
	}

	public void list(String path, List<String[]> l, int depth) {
		if (depth-- == 0) {
			return;
		}
		List<FolderItem> items = UpYunHelper.getClient().readDir(path);
		if (items == null) {
			return;
		}
		for (FolderItem i : items) {
			String p = path + "/" + i.name;
			if (i.type.equals("Folder")) {
				list(p, l, depth);
				l.add(new String[] { "d", p });
			} else {
				l.add(new String[] { "f", p });
			}
		}
	}
}
