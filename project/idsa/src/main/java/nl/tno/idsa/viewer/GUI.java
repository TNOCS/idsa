package nl.tno.idsa.viewer;

import nl.tno.idsa.framework.agents.Agent;
import nl.tno.idsa.framework.messaging.Messenger;
import nl.tno.idsa.framework.messaging.ProgressNotifier;
import nl.tno.idsa.framework.population.PopulationGenerator;
import nl.tno.idsa.framework.simulator.Sim;
import nl.tno.idsa.framework.utils.DataFinder;
import nl.tno.idsa.framework.utils.RandomNumber;
import nl.tno.idsa.framework.world.Environment;
import nl.tno.idsa.framework.world.Vertex;
import nl.tno.idsa.framework.world.World;
import nl.tno.idsa.framework.world.WorldGenerator;
import nl.tno.idsa.library.locations.PoliceSpawnPoint;
import nl.tno.idsa.library.population.PopulationDataNL;
import nl.tno.idsa.library.world.WorldModelNL;
import nl.tno.idsa.viewer.components.ProgressDialog;
import nl.tno.idsa.viewer.dialogs.SeasonSettingDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// TODO Document class. Mark it as the main class.
public class GUI {

    private static final boolean makeAgendas = true; // TODO This is a setting.

    public static void main(String[] args) throws Exception {

        ProgressDialog progressDialog = new ProgressDialog(null);
        ProgressNotifier.addObserver(progressDialog);

        Messenger.enableMirrorToConsole(true); // TODO Catch console messages in a graphical element.

        ProgressNotifier.notifyShowProgress(true);
        ProgressNotifier.notifyProgressMessage("Loading world data...");

        String path = DataFinder.pickDataSource();

        if (path == null) {
            System.out.println("No data files were found, exiting.");
            return;
        }

        World world = WorldGenerator.generateWorld(new WorldModelNL(),
                path + "/idsa_nav_network_pedestrian.shp",
                path + "/idsa_pand_osm_a_utm31n.shp",
                path + "/idsa_public_areas_a_utm31n.shp",
                path + "/idsa_vbo_utm31n.shp",
                path + "/idsa_pand_p_utm31n.shp");

        SeasonSettingDialog ssd = new SeasonSettingDialog(null, null);
        if (ssd.isCancelled()) {
            return;
        }

        ProgressNotifier.notifyProgressMessage("Creating environment...");
        Environment env = ssd.createEnvironmentWithSettings(world);

        ProgressNotifier.notifyProgressMessage("Creating population...");
        ProgressNotifier.notifyUnknownProgress();
        PopulationGenerator populationGenerator = new PopulationGenerator(env, new PopulationDataNL());
        List<Agent> population = populationGenerator.generatePopulation(path + "/idsa_cbs_buurten_utm31n.shp");
        env.setPopulation(population);

        ProgressNotifier.notifyProgressMessage("Creating agendas if needed...");
        env.initializePopulation(env.getSeason(), null, env.getDay(), env.getTime(), makeAgendas);

        // Add some police stations, randomly, as they are not in the world yet.
        // TODO Create police stations in the world.
        ProgressNotifier.notifyProgressMessage("Enriching environment...");
        List<Vertex> vertices = world.getVertices();
        int changedVertices = 0;
        while (changedVertices < 50) {
            Vertex randomVertex = vertices.get(RandomNumber.nextInt(vertices.size()));
            if (randomVertex.getArea() != null) {
                randomVertex.getArea().addFunction(new PoliceSpawnPoint());
                changedVertices++;
            }
        }
        ProgressNotifier.notifyProgress(100);

        // Hide progress
        ProgressNotifier.notifyShowProgress(false);
        ProgressNotifier.removeObserver(progressDialog);

        // Create sim
        Sim sim = Sim.getInstance();
        sim.init(env);

        // Open the viewer
        System.out.println("Creating viewer...");
        MainFrame mf = new MainFrame(sim);
        mf.show();

        // Start the sim
        System.out.println("Starting simulator...");
        sim.setXRealTime(30);
        sim.start();
    }
}


