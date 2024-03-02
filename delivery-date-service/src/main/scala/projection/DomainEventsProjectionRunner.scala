package projection

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ShardedDaemonProcess
import akka.persistence.r2dbc.state.scaladsl.R2dbcDurableStateStore
import akka.projection.ProjectionBehavior
import akka.projection.state.scaladsl.DurableStateSourceProvider

object DomainEventsProjectionRunner {

  def run(implicit system: ActorSystem[_]): Unit = {

    val numberOfSliceRanges: Int = system
      .settings
      .config
      .getInt("delivery-date-service.projections-slice-count")

    val sliceRanges = DurableStateSourceProvider.sliceRanges(
      system = system,
      durableStateStoreQueryPluginId = R2dbcDurableStateStore.Identifier,
      numberOfSliceRanges
    )

    ShardedDaemonProcess(system).init(
      name = "DomainEventsProjectionRunner",
      numberOfInstances = numberOfSliceRanges,
      behaviorFactory = index => ProjectionBehavior(DomainProjection.projection(sliceRanges(index)))
    )
  }
}
