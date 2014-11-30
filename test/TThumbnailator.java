

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import net.coobird.thumbnailator.Thumbnails;

public class TThumbnailator {
	public static void main(String[] args) throws IOException {
		File f = new File("C:/Users/yong/Desktop/avatars");
		List<String> avatars = new ArrayList<String>();
		if (f.isDirectory()) {
			for (File file : f.listFiles()) {
				Calendar c = Calendar.getInstance();
				int year = c.get(Calendar.YEAR);
				int month = c.get(Calendar.MONTH) + 1;
				String name = "/" + year + "/" + month + "/"
						+ c.getTimeInMillis() + ".jpg";
				Thumbnails.of(file).outputQuality(1).outputFormat("jpg")
						.size(35, 35)
						.toFile("C:/Users/yong/Desktop/avatars-1" + name);
				avatars.add(name);
			}
		}
		for (int i = 0; i < 10003; i++) {
			String avatar = avatars.get(i%avatars.size());
			System.out.println("update uc_member set avatar='"+avatar+"' where id="+(i+1)+";");
		}
	}
}
