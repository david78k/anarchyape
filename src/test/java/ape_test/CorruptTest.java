/**
  * Copyright (c) 2012 Yahoo! Inc. All rights reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. See accompanying LICENSE file.
*/


package ape_test;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ape.*;

import junit.framework.TestCase;

public class CorruptTest extends TestCase{
	
	public void testCorrupt(){
		File f1 = null;
		File f2 = null;
		try{
			
			
			 f1 = File.createTempFile("test_ori", ".tst");
			 f2 = File.createTempFile("test_final", ".tst");
			//File f2 = File.createTempFile("test_final", ".tst")
			FileOutputStream fos1 = new FileOutputStream(f1);
			FileOutputStream fos2 = new FileOutputStream(f2);

		    BufferedOutputStream out1 = new BufferedOutputStream(fos1);
		    BufferedOutputStream out2 = new BufferedOutputStream(fos2);
		    byte[]buf=new byte[20480];
		    for(int i=0;i<20480;i++)
		    	buf[i]=(byte)i;
		    out1.write(buf);
		    out2.write(buf);
		    out1.close();
		    out2.close();
		    fos1.close();
		    fos2.close();
		}
		catch(IOException e){
			e.printStackTrace();
			assertTrue(false);
		}
		
			
		
		   CorruptFileCommand c = new CorruptFileCommand();
		   c.size = 4096;
		   c.offset=0;
		   
		   try {
			   System.out.println("The path to corrupt is " + f2.getPath());
			   c.corrupt(f2.getPath());
		   } catch (IOException e) {
			// TODO Auto-generated catch block
				f1.deleteOnExit();
				f2.deleteOnExit();

			   e.printStackTrace();
				assertTrue(false);
		
		   }
		   
		   Process p2=null;
		   
		   int ret;
		   try {
			   p2 = new ProcessBuilder("diff", "-q", "test_ori.tst", "test_final.tst").start();
			   ret = p2.waitFor();
			   if(ret == 0) {
					f1.deleteOnExit();
					f2.deleteOnExit();

				   System.err.println("The two files are still the same");
				   assertTrue(false);
			   }
			   else
			   {
					System.out.println("The two files are diffrent");
					//System.out.println("Trying to remove the two testFiles");
					f1.deleteOnExit();
					f2.deleteOnExit();
					
					assertTrue(true);
			   }
		   } catch (IOException e) {
			   // TODO Auto-generated catch block
				f1.deleteOnExit();
				f2.deleteOnExit();

			   e.printStackTrace();
			   assertTrue(false);
		   } catch (InterruptedException e) {
			// TODO Auto-generated catch block
				f1.deleteOnExit();
				f2.deleteOnExit();

			e.printStackTrace();
			assertTrue(false);
		}	
		       
			   
	       
	       
	}

}
