package eu.monnetproject.clesa.core.commons;

public class ExampleThreading {

	/**
	 * @param args
	 */
	public static void main(String[] args) {	
		ExampleThreading example = new ExampleThreading();
		Barrier barrier = example.new Barrier(2);
		example.new SimpleThread("Jamaica", barrier).start();
		example.new SimpleThread2("Fiji", barrier).start();
		barrier.barrierWait();
		System.out.println("Acabamos");
	}


	public class SimpleThread extends Thread {
		private Barrier barrier;

		public SimpleThread(String str, Barrier barrier) {
			super(str);
			this.barrier = barrier;
		}

		public void run() {
			for (int i = 0; i < 10; i++) {
				System.out.println(i + " " + getName());
				try {
					sleep((int)(Math.random() * 1000));
				} catch (InterruptedException e) {}
			}
			System.out.println("DONE! " + getName());
			if(barrier!=null) 
				barrier.barrierPost();
		}
	}

	public class SimpleThread2 extends Thread {

		private Barrier barrier;

		public SimpleThread2(String str, Barrier barrier) {
			super(str);
			this.barrier = barrier;
		}
		public void run() {
			for (int i = 0; i < 2; i++) {
				System.out.println(i + " " + getName());
				try {
					sleep(10000);
				} catch (InterruptedException e) {}
			}
			System.out.println("DONE 2! " + getName());
			if(barrier!=null) 
				barrier.barrierPost();
		}
	}

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
}
