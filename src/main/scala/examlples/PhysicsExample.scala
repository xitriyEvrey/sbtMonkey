package examlples


import com.jme3.font.BitmapText
import com.jme3.app.SimpleApplication
import com.jme3.bullet.BulletAppState
import com.jme3.bullet.collision.{PhysicsCollisionEvent, PhysicsCollisionListener}
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.collision.{CollisionResult, CollisionResults}
import com.jme3.input.{KeyInput, MouseInput}
import com.jme3.input.controls.{ActionListener, AnalogListener, KeyTrigger, MouseButtonTrigger}
import com.jme3.material.Material
import com.jme3.math.{ColorRGBA, Ray, Vector3f}
import com.jme3.scene.{Geometry, SceneGraphVisitor, Spatial}
import com.jme3.scene.shape.{Box, Sphere}

import scala.util.Random

object PhysicsExample {
  def main(args: Array[String]): Unit = {
    val app = new PhysicsExample()
    app.start() // start the game

  }
}

class PhysicsExample extends SimpleApplication {

  def makeSphere(pos: Vector3f, radius: Float, name: String, color: ColorRGBA): Geometry = {
    val b = new Sphere(16, 16, radius) // create cube shape
    val box = new Geometry(name, b) // create cube geometry from the shape
    val mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md") // create a simple material
    mat.setColor("Color", color) // set color of material to blue

    box.setMaterial(mat) // set the cube's material
    box.setLocalTranslation(pos)
    rootNode.attachChild(box) // make the cube appear in the scene
    box
  }

  def makeBox(pos: Vector3f, size: Vector3f, name: String, color: ColorRGBA): Geometry = {
    val b = new Box(size.x, size.y, size.z) // create cube shape
    val box = new Geometry(name, b) // create cube geometry from the shape
    val mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md") // create a simple material
    mat.setColor("Color", color) // set color of material to blue

    box.setMaterial(mat) // set the cube's material
    box.setLocalTranslation(pos)
    rootNode.attachChild(box) // make the cube appear in the scene
    box
  }


  def makeRigid(g: Geometry, mass: Float): RigidBodyControl = {
    val phy = new RigidBodyControl(mass)
    g.addControl(phy)
    bulletAppState.getPhysicsSpace.add(phy)
    phy.setKinematicSpatial(mass == 0f)
    //    phy.setCcdSweptSphereRadius(.5f)
    phy
  }

  var bulletAppState: BulletAppState = _
  var floor: Geometry = _
  var wall1: Geometry = _
  var wall2: Geometry = _
  var wall3: Geometry = _
  var wall4: Geometry = _
  var wall5: Geometry = _

  var ball1: Geometry = _
  var ball2: Geometry = _
  var ball3: Geometry = _

  var sanitar: Geometry = _


  override def simpleInitApp(): Unit = {
    flyCam.setMoveSpeed(100f)
    bulletAppState = new BulletAppState()
    stateManager.attach(bulletAppState)

    floor = makeBox(new Vector3f(0f, -10.05f, 0f), new Vector3f(100f, 0.1f, 100f), "floor", ColorRGBA.Gray)
    wall1 = makeBox(new Vector3f(0f, 0f, 99.5f), new Vector3f(20f, 10f, 0.5f), "wall1", ColorRGBA.Red)
    wall2 = makeBox(new Vector3f(20f, 0f, 80f), new Vector3f(0.5f, 10f, 20f), "wall2", ColorRGBA.Blue)
    wall3 = makeBox(new Vector3f(-20f, 0f, 80f), new Vector3f(0.5f, 10f, 20f), "wall3", ColorRGBA.Blue)
    wall4 = makeBox(new Vector3f(15f, 0f, 60f), new Vector3f(5f, 10f, 0.5f), "wall4", ColorRGBA.Red)
    wall5 = makeBox(new Vector3f(-15f, 0f, 60f), new Vector3f(5f, 10f, 0.5f), "wall5", ColorRGBA.Red)


    makeRigid(floor, 0f)
    makeRigid(wall1, 0f)
    makeRigid(wall2, 0f)
    makeRigid(wall3, 0f)
    makeRigid(wall4, 0f)
    makeRigid(wall5, 0f)

     ball1 = makeSphere(new Vector3f(0f, 10.05f, 0f), 2.0f, "ball1", ColorRGBA.Yellow)
    makeRigid(ball1, 1f)

     ball2 = makeSphere(new Vector3f(20f, 10.05f, 0f), 2.0f, "ball2", ColorRGBA.Yellow)
    makeRigid(ball2, 1f)

     ball3 = makeSphere(new Vector3f(-20f, 10.05f, 20f), 2.0f, "ball3", ColorRGBA.Yellow)
    makeRigid(ball3, 1f)

     sanitar = makeSphere(new Vector3f(-20f, 10.05f, 20f), 4.0f, "sanitar", ColorRGBA.White)
    makeRigid(sanitar, 10f)

    /*val max = 0
    for (i <- 0 until max; j <- 0 until i) {
      val y = (max - i.toFloat) - 10
      val x = (max - i) / 2f + j
      val b = makeBox(new Vector3f(x, y, 0f), new Vector3f(.5f, 0.5f,.5f + (max - i) / 20.0f), s"box $i $j", new ColorRGBA(math.random().toFloat, math.random().toFloat, math.random().toFloat, 1f))
      makeRigid(b, 1f)
    }*/

    initShooting()
  }


  def initShooting(): Unit = {
    setDisplayStatView(false)
    guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt")
    val ch = new BitmapText(guiFont, false)
    ch.setSize(guiFont.getCharSet.getRenderedSize * 2 toFloat)
    ch.setText("+") // crosshairs

    ch.setLocalTranslation( // center
      settings.getWidth / 2 - ch.getLineWidth / 2, settings.getHeight / 2 + ch.getLineHeight / 2, 0)
    guiNode.attachChild(ch)

    inputManager.addMapping("up", new KeyTrigger(KeyInput.KEY_E), new MouseButtonTrigger(MouseInput.BUTTON_RIGHT))
    inputManager.addMapping("shoot", new KeyTrigger(KeyInput.KEY_SPACE), new MouseButtonTrigger(MouseInput.BUTTON_RIGHT))

    inputManager.addMapping("left", new KeyTrigger(KeyInput.KEY_H), new MouseButtonTrigger(MouseInput.BUTTON_LEFT))
    inputManager.addMapping("down", new KeyTrigger(KeyInput.KEY_J), new MouseButtonTrigger(MouseInput.BUTTON_RIGHT))
    inputManager.addMapping("right", new KeyTrigger(KeyInput.KEY_K), new MouseButtonTrigger(MouseInput.BUTTON_RIGHT))
    inputManager.addMapping("forward", new KeyTrigger(KeyInput.KEY_I), new MouseButtonTrigger(MouseInput.BUTTON_RIGHT))

    inputManager.addListener(new ActionListener {
      override def onAction(name: String, isPressed: Boolean, tpf: Float): Unit = {
        if (isPressed) {
          println("shoot")
          val ball = makeSphere(cam.getLocation, 1f, "ball", ColorRGBA.Green)
          val ballPhy = makeRigid(ball, 5f)
          ballPhy.setLinearVelocity(cam.getDirection().normalize().mult(100f))
          ball.setUserData("phy", ballPhy)
        }
      }
    }, "shoot")




    inputManager.addListener(new AnalogListener {
      override def onAnalog(name: String, value: Float, tpf: Float): Unit = {
        rootNode.depthFirstTraversal((spatial: Spatial) => {
          if (spatial.getName == "ball") {
            val rb = spatial.getUserData("phy").asInstanceOf[RigidBodyControl]
            //              rb.applyImpulse(new Vector3f(0f, rb.getMass, 0f).mult( 20f * value), new Vector3f(0f, 0f, 0f))
            rb.applyForce(new Vector3f(0f, rb.getMass, 0f).mult(20f), new Vector3f(0f, 0f, 0f))
          }
        })
      }
    }, "up")




    inputManager.addListener(new ActionListener {
      override def onAction(name: String, isPressed: Boolean, tpf: Float): Unit = {
        if (isPressed && name == "left") {
          sanitar.getControl(classOf[RigidBodyControl]).setLinearVelocity(new Vector3f(0f, 10.05f, 0f) )
        }
      }
    }, "left")






    bulletAppState.getPhysicsSpace.addCollisionListener((event: PhysicsCollisionEvent) => {
      val a = event.getNodeA
      val b = event.getNodeB

      if (a.getName.startsWith("box") && b.getName == "ball" && a.isInstanceOf[Geometry]) {
        a.asInstanceOf[Geometry].getMaterial.setColor("Color", ColorRGBA.Red)
      }
      if (b.getName.startsWith("box") && a.getName == "ball" && b.isInstanceOf[Geometry]) {
        b.asInstanceOf[Geometry].getMaterial.setColor("Color", ColorRGBA.Red)
      }
    })
  }

  override def simpleUpdate(tpf: Float): Unit = {
    // geom.setLocalTranslation(geom.getLocalTranslation.add(new Vector3f(tpf, tpf, tpf)))
  }
}


