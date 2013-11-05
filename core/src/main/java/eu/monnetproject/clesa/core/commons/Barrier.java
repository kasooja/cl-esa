package eu.monnetproject.clesa.core.commons;


public class Barrier {
		/** Number of objects being waited on */
		private int counter;
		/** Constructor for Barrier
		 * 
		 * @param n Number of objects to wait on
		 */
		public Barrier(int n) {
			counter = n;
		}
		/** Wait for objects to complete */
		public synchronized void barrierWait() {
			while(counter > 0) {
				try {
					wait();
				} catch (InterruptedException e) {}
			}
		}
		/** Object just completed */
		public synchronized void barrierPost() {
			counter--;
			if(counter == 0) {
				notifyAll();
			}
		}
		public boolean isFinished() {
			return counter ==0 ? true : false;
		}
	}
