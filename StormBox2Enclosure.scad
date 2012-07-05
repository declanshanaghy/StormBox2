$fn=12;

//Box wall thickness
WALL_T=1;

//Standoff dimensions
STAND_R=8.0;
STAND_HOLE_R=1.0;

//ProMicro board dimensions
MICRO_H_DIM=[38, 35.5, 0];
MICRO_H_OFFSET=4;
MICRO_STAND_H=5;

//Trigger board dimensions
TRIG_H_DIM=[57, 53.5, 0];
TRIG_H_OFFSET=2.5;
TRIG_THICK=0;
TRIG_DIM=[TRIG_H_DIM[0], TRIG_H_DIM[1]+TRIG_H_OFFSET*2, TRIG_THICK];
TRIG_STAND_H=30;
TRIG_COMP_H=11;

//BOX Bottom Dimensions
BOX_X_SPACE=0;
BOX_Y_SPACE=30;
BOX_BOTTOM_INSIDE_D = [TRIG_DIM[0]+BOX_X_SPACE+STAND_R, TRIG_DIM[1]+BOX_Y_SPACE+STAND_R/2, TRIG_STAND_H+TRIG_COMP_H+TRIG_THICK];
BOX_BOTTOM_OUTSIDE_D = [BOX_BOTTOM_INSIDE_D[0]+WALL_T*2, BOX_BOTTOM_INSIDE_D[1]+WALL_T*2, BOX_BOTTOM_INSIDE_D[2]+WALL_T];
BOX_TOP_H=5+WALL_T;
BOX_TOP_CLR=0.5;

boxTop();
!boxBottomComplete();

module boxBottomComplete() {
	union() {
		boxBottom();
		trigStands();
		microStands();
	}
}

module boxTop() {
	translate([-STAND_R/2-WALL_T-BOX_TOP_CLR/2, -STAND_R/2-WALL_T-BOX_TOP_CLR/2, -BOX_TOP_H-WALL_T])
	difference() {
		translate([-BOX_X_SPACE/2-WALL_T, -BOX_Y_SPACE/2-WALL_T, -BOX_TOP_H-WALL_T])
			cube([BOX_BOTTOM_OUTSIDE_D[0]+WALL_T*2+BOX_TOP_CLR, BOX_BOTTOM_OUTSIDE_D[1]+WALL_T*2+BOX_TOP_CLR, BOX_TOP_H]);
		translate([-BOX_X_SPACE/2, -BOX_Y_SPACE/2, -BOX_TOP_H])
			cube([BOX_BOTTOM_OUTSIDE_D[0]+BOX_TOP_CLR, BOX_BOTTOM_OUTSIDE_D[1]+BOX_TOP_CLR, BOX_TOP_H]);
    }
}

module boxBottom() {
	translate([-STAND_R/2, -STAND_R/2, 0])
    union() {
		difference() {
			translate([-WALL_T-BOX_X_SPACE/2, -WALL_T-BOX_Y_SPACE/2, -WALL_T])
				cube([BOX_BOTTOM_OUTSIDE_D[0], BOX_BOTTOM_OUTSIDE_D[1], BOX_BOTTOM_OUTSIDE_D[2]]);
			translate([-BOX_X_SPACE/2, -BOX_Y_SPACE/2, 0])
				cube([BOX_BOTTOM_INSIDE_D[0], BOX_BOTTOM_INSIDE_D[1], BOX_BOTTOM_INSIDE_D[2]+WALL_T*2]);
	    }
	
		translate([BOX_BOTTOM_INSIDE_D[0]/2-STAND_R/2, -BOX_Y_SPACE/2, 0])
			boxStand();
	
		translate([BOX_BOTTOM_INSIDE_D[0]/2-STAND_R/2, BOX_BOTTOM_INSIDE_D[1]-BOX_Y_SPACE/2-STAND_R, 0])
			boxStand();
    }
}

module boxStand() {
	difference() {
		cube([STAND_R, STAND_R, BOX_BOTTOM_INSIDE_D[2]]);
		translate([STAND_R/2, STAND_R/2, -0.01])
			cylinder(r=STAND_HOLE_R, h=BOX_BOTTOM_INSIDE_D[2]+WALL_T+0.02);
	}
}

module microStands() {
	translate([-STAND_R/2, -STAND_R/2, 0])
    union() {
		microStand();
//		translate([0, MICRO_H_DIM[1], 0])
//			microStand();
		translate([MICRO_H_DIM[0], 0, 0])
			microStand(cylinder=false);
		translate([MICRO_H_DIM[0], MICRO_H_DIM[1], 0])
			microStand(cylinder=false);
    }
}

module microStand(cylinder=false) {
	difference() {
		if ( cylinder ) {
			translate([STAND_R/2, STAND_R/2, 0])
				cylinder(r=STAND_R, h=MICRO_STAND_H);
		}
		else
			cube([STAND_R, STAND_R, MICRO_STAND_H]);
		translate([STAND_R/2, STAND_R/2, -0.01])
			cylinder(r=STAND_HOLE_R, h=MICRO_STAND_H+0.02);
	}
}

module trigStands() {
	translate([-STAND_R/2, -STAND_R/2, 0])
    union() {
		translate([0, TRIG_H_DIM[1], 0])
			trigStand();
		translate([TRIG_H_DIM[0], 0, 0])
			trigStand();
		translate([TRIG_H_DIM[0], TRIG_H_DIM[1], 0])
			trigStand();
    }
}

module trigStand() {
	difference() {
		cube([STAND_R, STAND_R, TRIG_STAND_H]);
		translate([STAND_R/2, STAND_R/2, -0.01])
			cylinder(r=STAND_HOLE_R, h=TRIG_STAND_H+0.02);
	}
}
